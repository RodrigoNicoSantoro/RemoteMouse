import java.io.IOException;
import java.net.*;

public class Main {

    private static final int SERVER_PORT = 9898;

    private static ServerSocket serverSocket = null;
    private static Socket socket = null;

    public static void main(String[] args) throws Exception {
        System.out.println("The home server is running on " + getIPAddress());
        createServer();
        listenToNewConnections();
    }

    private static String getIPAddress() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String address = socket.getLocalAddress().toString().replace("/", "");
            socket.close();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "not found";
    }

    private static void createServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot create server");
        }
    }

    private static void listenToNewConnections() {
        while (true) {
            try {
                socket = serverSocket.accept();
                // new thread for a client
                new HomeServer(socket).start();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("I/O error: " + e);
            }
        }
    }
}