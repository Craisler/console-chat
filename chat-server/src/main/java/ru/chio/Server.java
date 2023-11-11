package ru.chio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private final AuthenticationProvider authenticationProvider;

    public Server(int port, AuthenticationProvider authenticationProvider) {
        this.port = port;
        clients = new ArrayList<>();
        this.authenticationProvider = authenticationProvider;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Сервер запущен на порту " + port);
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
    clients.add(clientHandler);
    broadcastMessage("Клиент: " + clientHandler.getUsername() + " вошел в чат");
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized void sendMessageToUser(String user, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(user)) {
                System.out.println("Сообщение клиенту: " + client.getUsername());
                client.sendMessage(message);
            }
        }
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Клиент: " + clientHandler.getUsername() + "вышел из чата");

    }
    public synchronized List<String> getUserList() {
//        var listUsers = new ArrayList<String>();
//        for (ClientHandler client : clients) {
//            listUsers.add(client.getUsername());
//        }
//        return listUsers;
        return clients.stream()
                .map(ClientHandler::getUsername)
//                .map(client -> client.getUsername())
                .collect(Collectors.toList());
    }
    public synchronized boolean kickUser(String user, String whoDoes) {
        System.out.println("Отключает пользователь: "+ whoDoes);
        for (ClientHandler client : clients) {
            System.out.println("User:" + client.getUsername());
            if (client.getUsername().equals(user)){
                System.out.println("kick user: "+client.getUsername());
                client.sendMessage("Вас отключают");
                client.disconnect();
                return true;
            }
        }
        return false;
    }
}
