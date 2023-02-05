package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    Server() {
        try {
            ServerSocket server = new ServerSocket(22000);
            ArrayList<ClientHandler> cl = new ArrayList<>();
            Thread clientsHandler = new Thread() {
                @Override
                public void run() {
                    int count = 0;
                    while (true) {
                        try {
                            Socket client = server.accept();
                            ClientHandler clientHandle = new ClientHandler(client);
                            synchronized (cl) {
                                cl.add(clientHandle);
                            }
                            clientHandle.start();
                            System.out.println("Client " + count + " Connected");
                            ClientHandler c = cl.get(count);
                            c.sendMsg("Your id : " + count);
                            count++;
                        } catch (IOException e) {

                        }
                    }
                }
            };
            clientsHandler.start();
            Thread messageHandler = new Thread() {
                ArrayList<String> msgs;

                @Override
                public void run() {
                    while (true) {
                        synchronized (cl) {
                            for (int i = 0; i < cl.size(); i++) {
                                try {
                                    ClientHandler cTmp = cl.get(i);
                                    msgs = cTmp.getMessages();
                                    if (!msgs.isEmpty()) {
                                        synchronized (msgs) {
                                            for (int j = 0; j < msgs.size(); j++) {
                                                String msg = msgs.get(j);
                                                // Change target with sender
                                                String sTarget = "";
                                                String sMsg = "";
                                                int count = 0;
                                                boolean a = false;
                                                for (int k = 0; k < msg.length(); k++) {
                                                    if (msg.charAt(k) == '@' && !a) {
                                                        // msg += '@';
                                                        count++;
                                                        a = true;
                                                    } else if (msg.charAt(k) != '@' && count == 0)
                                                        sTarget += msg.charAt(k);
                                                    else if (count == 1)
                                                        sMsg += msg.charAt(k);
                                                }
                                                int target = Integer.parseInt(sTarget);
                                                cl.get(target).sendMsg(i + "@" + sMsg);
                                            }

                                            msgs.clear();
                                        }
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            messageHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
