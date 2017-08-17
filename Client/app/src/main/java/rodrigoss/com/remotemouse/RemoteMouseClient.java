package rodrigoss.com.remotemouse;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class RemoteMouseClient {

    private Socket socket;

    private DataInputStream in;
    private PrintWriter out;


    public RemoteMouseClient() {
        socket = new Socket();
    }

    public void connectToServer(String serverAddress) throws IOException {
        connectSocket(serverAddress);
        openStreams();
    }

    private void connectSocket(String serverAddress) throws IOException {
        socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(serverAddress, 9898);
        try {
            socket.connect(socketAddress, 2000);
        } catch(Exception e){
            disconnectFromServer();
            throw e;

        }
    }

    private void openStreams() throws IOException {
        if(socket.isConnected()){
            in = new DataInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            //System.out.println(in.readUTF());
        }
    }

    public void disconnectFromServer() throws IOException {
        socket.close();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void sendCommand(String command) {
        if(isConnected()) {
            out.println(command);
        }
    }
}