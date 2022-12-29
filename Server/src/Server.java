import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;
    private String ip;
    private final int PORT = 7000;
    private int numberOfFiles;
    private final String pathOfFiles = "./Server/project-files/";

    public static void main(String[] args) {
        //Server server = new Server();
        //server.init();  //maybe do initA() and initB() --> multithreaded
        calculateFilesToSend(1,3,"A");
    }

    public Server() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            this.serverSocket = new ServerSocket(this.PORT);
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ip = socket.getLocalAddress().getHostAddress();
            System.out.println("Server is running on " + ip + ":" + this.PORT);

            this.numberOfFiles = getNumberOfFiles();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void init() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                /**
                 * The first thing the client sends is the String "n_X X", where X='A' or X='B'.
                 * We send X to notify to the server the order of sending the files. After the server understands its identity
                 * it can calculate which files are needed to send, so they don't overlap with server_B's.
                 * The client sends this message to both servers when initiated
                 */
                String[] client_info = ((String) ois.readObject()).split(" ");
                int n_A = Integer.parseInt(client_info[0]);
                int n_B = Integer.parseInt(client_info[1]);
                String server_id = client_info[2];
                ArrayList<String> file_names = calculateFilesToSend(n_A, n_B, server_id);  //names of files to send
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public int getNumberOfFiles() {
        try {
            return new File(this.pathOfFiles).list().length;
        } catch (Exception e) {
            System.err.println(e);
            return -1;
        }
    }

    public static ArrayList<String> calculateFilesToSend(int n_a, int n_b, String id) {
        ArrayList<String> file_indexes = new ArrayList<String>();

        if (id == "A") {
            //if it's server_A it sends the first files n_a files
            for (int i = 1; i <= 160; i+=n_b+1) {
                String index = i < 100 ? i < 10? "00"+i : "0"+i : String.valueOf(i);
                file_indexes.add("s" + index + ".m4s");
            }
        } else if (id == "B") {
            //if it's server_B it means server_A sends the first n_a files, so server_B must calculate the rest


        } else {
            System.err.println("Unknown id for server");
            return null;
        }

        return null;
    }

}