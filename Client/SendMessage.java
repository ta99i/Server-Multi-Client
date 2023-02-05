package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SendMessage extends Thread {
    Socket _server;
    Scanner _scanner;
    String _buffer;
    DataOutputStream serverWritStream;
    HashMap<Integer, DH> _tmpClientToKeys;
    HashMap<Integer, String> _messageToCrypt;
    ArrayList<Integer> _waitOtherPublicKey;
    ArrayList<Integer> _messageToCryptID;

    SendMessage(Socket server) {
        _tmpClientToKeys = new HashMap<>();
        _waitOtherPublicKey = new ArrayList<>();
        _messageToCrypt = new HashMap<>();
        _messageToCryptID = new ArrayList<>();
        this._server = server;
        this._scanner = new Scanner(System.in);
        try {
            serverWritStream = new DataOutputStream(server.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                _buffer = "";
                _buffer = _scanner.nextLine();

                ArrayList<String> _bufferSplited = BufferSplit(_buffer);
                int id = Integer.parseInt(_bufferSplited.get(0));

                switch (_bufferSplited.get(1)) {
                    case "HS":
                        String _generateHandSHake = GenerateHandShake(id);
                        if (_generateHandSHake == "FALSE")
                            continue;
                        else
                            _buffer = _generateHandSHake;
                        break;
                    case "PRIVATE":
                        synchronized (_messageToCrypt) {
                            _messageToCrypt.put(id, _bufferSplited.get(2));
                        }
                        synchronized (_messageToCryptID) {
                            _messageToCryptID.add(id);
                        }
                        break;
                }
                serverWritStream.writeUTF(_buffer);
                if (_buffer == "EXIT")
                    break;
            }
            serverWritStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String s) {
        try {
            serverWritStream.writeUTF(s);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * BufferSplit
     * 
     * @Explain Splite Buffer to be readbal for client
     * 
     * @Paramater String as id@command@paramater
     * 
     * @Return ArrayList of 3 String id,command,paramater
     *
     */
    public ArrayList<String> BufferSplit(String buffer) {
        ArrayList<String> commands = new ArrayList<>();
        String str = "";
        int count = 0;
        for (int i = 0; i < buffer.length(); i++) {
            if ((buffer.charAt(i) == '@' && count < 2)) {
                count++;
                commands.add(str);
                str = "";
            } else
                str += buffer.charAt(i);
        }
        commands.add(str);
        return commands;
    }

    public String GenerateHandShake(int id) {
        DH dh = new DH();
        if (AddKeysToClient(id, dh) && WaitOtherPublicKey(id)) {
            return id + "@HS@" + dh._generator + "@" + dh._prime + "@" + dh._publicKey;
        }
        return "FALSE";
    }

    private boolean AddKeysToClient(int id, DH dh) {
        synchronized (_tmpClientToKeys) {
            if (_tmpClientToKeys.containsKey(id))
                _tmpClientToKeys.remove(id);
            _tmpClientToKeys.put(id, dh);
            if (_tmpClientToKeys.get(id) == dh)
                return true;
        }
        return false;
    }

    private boolean WaitOtherPublicKey(int id) {
        synchronized (_waitOtherPublicKey) {
            int index = _waitOtherPublicKey.indexOf(id);
            if (index != -1)
                _waitOtherPublicKey.remove(index);
            _waitOtherPublicKey.add(id);
            if (_waitOtherPublicKey.contains(id))
                return true;
        }
        System.out.println("False");
        return false;
    }

    public ArrayList<Integer> getWaitedClient() {
        synchronized (_waitOtherPublicKey) {
            return _waitOtherPublicKey;
        }
    }

    public void ClearWaitedClient() {
        synchronized (_waitOtherPublicKey) {
            _waitOtherPublicKey.clear();
        }
    }

    public DH GetKeys(int id) {
        synchronized (_tmpClientToKeys) {
            DH d = _tmpClientToKeys.get(id);
            _tmpClientToKeys.remove(id);
            return d;
        }
    }

    public HashMap<Integer, String> GetMessageToCrypt() {
        synchronized (_messageToCrypt) {
            return _messageToCrypt;
        }
    }

    public ArrayList<Integer> GetMessageToCryptID() {
        synchronized (_messageToCryptID) {
            return _messageToCryptID;
        }
    }

    public void ClearMessageToCrypt() {
        synchronized (_messageToCrypt) {
            _messageToCrypt.clear();
        }
    }

    public void ClearMessageToCryptID() {
        synchronized (_messageToCryptID) {
            _messageToCryptID.clear();
        }
    }
}
