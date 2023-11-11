package ru.chio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

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
//        server.subscribe(this);
        new Thread(() -> {
            try {
                authenthifacateUser(server);
                communicateWithUser(server);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                disconnect();
            }
        }).start();
    }

    private void authenthifacateUser(Server server) throws IOException {
        boolean isAuthenthifacate = false;
        while (!isAuthenthifacate) {
            String message = in.readUTF();
//            /auth login JPasswordField
//            /sign login nick password
            String[] args = message.split(" ");
            String command = args[0];
            switch (command) {
                case "/auth": {
                    String login = args[1];
                    String password = args[2];
                    String username = server.getAuthenticationProvider().getUsernameByLoginAndPassword(login, password);
                    if (username == null || username.isBlank()) {
                        sendMessage("Указан неверный логин/пароль");
                    } else {
                        this.username = username;
                        sendMessage(username + "добро пожаловать в чат!");
                        server.subscribe(this);
                        isAuthenthifacate = true;
                    }
                    break;
                }
                case "/register": {
                    String login = args[1];
                    String nick = args[2];
                    String password = args[3];
                    boolean isRegistred = server.getAuthenticationProvider().register(login, password, nick);
                    if (isRegistred) {
                        sendMessage("Указан неверный логин/пароль");
                    } else {
                        this.username = nick;
                        sendMessage(username + "добро пожаловать в чат!");
                        server.subscribe(this);
                        isAuthenthifacate = true;
                    }
                    break;
                }
                default: {
                    sendMessage("Авторизуйтесь");
                }
            }
        }
    }

    private void communicateWithUser(Server server) throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith("/")) {
                if (message.equals("/w")) {
                    System.out.println("w");
                    String user = message.replaceAll("^/w\\s+(\\w+)\\s+.+", "$1");
                    message = message.replaceAll("^/w\\s+(\\w+)\\s+(.+)", "$2");
                    System.out.println("user = " + user + " message = " + message);
                    server.sendMessageToUser(user, message);
                }
                if (message.equals("/exit")) {
                    break;
                } else if (message.equals("/list")) {
                    List<String> userList = server.getUserList();
                    String joinedUsers =
                            String.join(", ", userList);
//                            userList.stream().collect(Collectors.joining(","));
                    sendMessage(joinedUsers);
                }
            } else {
                server.broadcastMessage("Server: " + message);
            }
        }
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