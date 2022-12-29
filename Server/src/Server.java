import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private String ip;
    private final int PORT = 7000;

    public static void main(String[] args) {
        Server server = new Server();
        server.init();
    }

    public Server() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            this.serverSocket = new ServerSocket(this.PORT);
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ip = socket.getLocalAddress().getHostAddress();

            System.out.println("Server is running on " + ip + ":" + this.PORT);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void init() {
        try {

            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            /**
             * the first thing the client sends is "n_A n_B IP_A IP_B"
             * the client sends this message to both servers when initiated
             */
            String client_info = (String) ois.readObject();

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}