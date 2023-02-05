package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class RecivedMessage extends Thread {
    Socket _server;
    DataInputStream serverReadSource;
    HashMap<Integer, DH> _clientToKeys;
    ArrayList<String> _msgToSend;

    RecivedMessage(Socket server) {
        this._server = server;
        this._msgToSend = new ArrayList<>();
        this._clientToKeys = new HashMap<>();

        try {
            serverReadSource = new DataInputStream(server.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String _buffer;
        // Read id from server
        try {
            _buffer = serverReadSource.readUTF();
            System.out.println(_buffer);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        while (true) {
            try {
                _buffer = serverReadSource.readUTF();
                ArrayList<String> _bufferSplited = BufferSplit(_buffer);
                int id = Integer.parseInt(_bufferSplited.get(0));

                switch (_bufferSplited.get(1)) {
                    case "HS":
                        System.out.println("Client " + _bufferSplited.get(0) +
                                " > Asking for Hand Shake !");

                        ArrayList<Integer> params = GetParamHS(_bufferSplited.get(2));
                        if (HandShake(id, params)) {
                            System.out.println("Generated shared key seccsufuly !");
                            if (GetPublicKey(id) > 0) {
                                if (AddMessageToSend(id + "@SHS@" + GetPublicKey(id))) {
                                    System.out.println("Wait client " + id + " to recive public key !");
                                } else {
                                    System.out.println("Failed To Add Message to LessageList");
                                }
                            } else {
                                System.out.println("Failed to get public key");
                            }
                        } else {
                            System.out.println("1 || 0");
                        }
                        break;
                    case "SHS":
                        int publicKey = Integer.parseInt(_bufferSplited.get(2));
                        if (publicKey > 0) {
                            System.out.println("Recived Public key from Client " +
                                    _bufferSplited.get(0));
                            if (UpdateKeysToClient(id, publicKey))
                                System.out.println("Generated shared key seccsufuly !");
                        } else {
                            System.out.println("Failed to recive public key");
                        }
                        break;
                    case "PRIVATE":
                        int sharedkey = (_clientToKeys.get(id))._sharedKey;
                        String res = ZigZag.DeCrypte(_bufferSplited.get(2), sharedkey);
                        System.out.println(res);
                        break;
                }

                RecivedMessage.sleep(10);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
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

    /*
     * getParamHS
     * 
     * @Explain Splite param to be readbol
     * 
     * @Paramater String as genetrator@prime@otherPublicKey
     * 
     * @Return ArrayList of 3 Integer genetrator,prime,otherPublicKey
     */
    public ArrayList<Integer> GetParamHS(String param) {
        ArrayList<Integer> paramater = new ArrayList<>();
        String p = "";
        for (int i = 0; i < param.length(); i++) {
            if (param.charAt(i) == '@') {
                paramater.add(Integer.parseInt(p));
                p = "";
            } else
                p += param.charAt(i);
        }
        paramater.add(Integer.parseInt(p));
        return paramater;
    }

    /* */
    private boolean HandShake(int id, ArrayList<Integer> params) {
        DH dh = new DH(params.get(0), params.get(1), params.get(2));
        if (dh._sharedKey == 0 || dh._sharedKey == 1)
            return false;
        if (!AddKeysToClient(id, dh)) {
            System.out.println("Failed to add keys to List");
            return false;
        }
        return true;
    }

    public boolean AddKeysToClient(int id, DH dh) {
        synchronized (_clientToKeys) {
            if (_clientToKeys.containsKey(id))
                _clientToKeys.remove(id);
            _clientToKeys.put(id, dh);
            if (_clientToKeys.get(id) == dh)
                return true;
        }

        return false;
    }

    public boolean UpdateKeysToClient(int id, int publicKey) {
        synchronized (_clientToKeys) {
            DH newDH = _clientToKeys.get(id);
            newDH.calcSharedKey(publicKey);
            _clientToKeys.remove(id);
            _clientToKeys.put(id, newDH);
            if ((_clientToKeys.get(id))._sharedKey == newDH._sharedKey)
                return true;
        }
        return false;
    }

    private boolean AddMessageToSend(String buffer) {
        boolean isExist = false;
        synchronized (_msgToSend) {
            _msgToSend.add(buffer);
            if (_msgToSend.contains(buffer))
                isExist = true;
        }
        return isExist;
    }

    public ArrayList<String> GetMessage() {
        synchronized (_msgToSend) {
            ArrayList<String> msg = _msgToSend;
            return msg;
        }
    }

    public void ClearMessage() {
        synchronized (_msgToSend) {
            _msgToSend.clear();
        }
    }

    private int GetPublicKey(int id) {
        int key;
        synchronized (_clientToKeys) {
            key = (_clientToKeys.get(id))._publicKey;
        }
        return key;
    }

    public String GetToCrypted(int id, String str) {
        return (_clientToKeys.get(id))._sharedKey + "";
    }

    public boolean MessageToCrypt(int id, String str) {
        ArrayList<String> _splitedmessage = BufferSplit(str);
        String buffer = _splitedmessage.get(2);
        int key;
        synchronized (_clientToKeys) {
            key = (_clientToKeys.get(id))._sharedKey;
        }
        String bb = ZigZag.GenerateRandomString(1024 - buffer.length());
        String resultat = ZigZag.Cryptage(buffer.length() + "@" + str + bb, key);
        System.out.println(resultat);
        if (AddMessageToSend(id + "@PRIVATE@" + resultat))
            return true;
        return false;
    }

    public boolean MessageToDecrypt(String s) {
        // Get length of res
        String p = "";
        int i = 0;
        while (s.charAt(i) != '@') {
            p += s.charAt(i);
            i++;
        }
        int lm = Integer.parseInt(p);
        System.out.println(lm);
        int count = 0;
        /*
         * while (count < 2) {
         * if (s.charAt(i) == '@')
         * count++;
         * i++;
         * }
         * for (int j = i; j < i + lm; j++) {
         * System.out.print(s.charAt(j));
         * }
         */
        return true;
    }
}
