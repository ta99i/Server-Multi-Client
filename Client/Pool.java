package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Pool {
    Pool(Socket socket) {
        RecivedMessage recivedMessage = new RecivedMessage(socket);
        SendMessage sendMessage = new SendMessage(socket);
        recivedMessage.start();
        sendMessage.start();
        while (true) {
            ArrayList<String> msgToSend;
            ArrayList<Integer> waited = sendMessage.getWaitedClient();
            HashMap<Integer, String> _messageToCrypt;
            ArrayList<Integer> _messageToCryptID;
            if (!waited.isEmpty())
                synchronized (waited) {
                    for (int i : waited) {
                        DH d = sendMessage.GetKeys(i);
                        if (recivedMessage.AddKeysToClient(i, d))
                            System.out.println("Seccusfuly add key");
                        else {
                            System.out.println("Failed to add key");
                        }
                    }
                    sendMessage.ClearWaitedClient();
                }
            msgToSend = recivedMessage.GetMessage();
            if (!msgToSend.isEmpty()) {
                synchronized (msgToSend) {
                    for (String str : msgToSend) {
                        sendMessage.send(str);
                    }
                    msgToSend.clear();
                    recivedMessage.ClearMessage();
                }
            }
            _messageToCrypt = sendMessage.GetMessageToCrypt();
            _messageToCryptID = sendMessage.GetMessageToCryptID();
            if (!_messageToCryptID.isEmpty()) {
                for (int i : _messageToCryptID) {
                    System.out.println(i + "@PRIVATE@" + _messageToCrypt.get(i));
                    recivedMessage.MessageToCrypt(i, i + "@PRIVATE@" + _messageToCrypt.get(i));
                }
                sendMessage.ClearMessageToCrypt();
                sendMessage.ClearMessageToCryptID();
            }
            /*
             * _clientToKeys = recivedMessage.GetKeysToClient();
             * if (!_clientToKeys.isEmpty()) {
             * synchronized (_clientToKeys) {
             * sendMessage.UpdateKeysToClient(_clientToKeys);
             * }
             * }
             */
        }
    }

    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Socket server = new Socket(ip, 22000);
            new Pool(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
