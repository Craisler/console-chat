package ru.chio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    private String username;

    private static int userCount = 0;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        username = "User" + userCount++;
        server.subscribe(this);
        new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.equals("/w")) {
                            System.out.println("w");
                            String user = message.replaceAll("^/w\\s+(\\w+)\\s+.+","$1");
                            message =  message.replaceAll("^/w\\s+(\\w+)\\s+(.+)","$2");
                            System.out.println("user = "+user+ " message = "+message);
                            server.sendMessageToUser(user, message);
                        }
                        if (message.equals("/exit")) {
                            break;
                        }
                    }
                    server.broadcastMessage("Server:" + message);

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }

        }).start();

    }


    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }
}
