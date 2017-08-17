import java.awt.AWTException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HomeServer extends Thread {

    protected Socket socket;
    private CommandInterpreter interpreter;

    private String clientIP;

    private InputStream inp = null;
    private BufferedReader brinp = null;
    private DataOutputStream out = null;

    public HomeServer(Socket clientSocket) throws AWTException, IOException {
        this.socket = clientSocket;
        interpreter = new CommandInterpreter();
        openStreams();
        clientIP = socket.getInetAddress().toString().replace("/","");
        System.out.println("Connected to " + clientIP);
    }

    private void openStreams() throws IOException {
        inp = socket.getInputStream();
        brinp = new BufferedReader(new InputStreamReader(inp));
        out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF("Thank you for connecting to " + socket.getLocalSocketAddress());
    }

    public void run() {
        String input;
        while (true) {
            try {
                input = brinp.readLine();
                if (input == null || input.equals("."))
                    break;
                System.out.println(input);
                interpreter.interpretCommand(input);
            } catch (IOException e) {
                System.out.println(clientIP + " disconnected.");
                closeSocket();
                //e.printStackTrace();
                return;
            }

        }
        closeSocket();
    }

    private void closeSocket() {
        try {
            inp.close();
            brinp.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}