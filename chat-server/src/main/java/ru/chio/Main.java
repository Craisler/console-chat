package ru.chio;

public class Main {
    public static void main(String[] args) {
int port = 8080;
if (args.length >= 1){
    port = Integer.parseInt(args[0]);
}
        Server server = new Server(8089);

        server.start();
    }

}