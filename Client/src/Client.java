import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private Socket socket_A, socket_B;
    private ObjectInputStream reader_A, reader_B;
    private ObjectOutputStream writer_A, writer_B;
    private int n_A, n_B, PORT = 7000;  //default port to 7000
    private String IP_A, IP_B;
    private final String pathOfFiles = "./Client/files/";
    private Map<Character, Double> serverToTime;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Not enough parameters specified");
            System.exit(0);
        }

        int n_A = Integer.parseInt(args[0]);
        int n_B = Integer.parseInt(args[1]);
        String IP_A = args[2];
        String IP_B = args[3];

        System.out.println("Client parameters");
        System.out.println("n_A: " + n_A);
        System.out.println("n_B: " + n_B);
        System.out.println("IP_A: " + IP_A);
        System.out.println("IP_B: " + IP_B);

        Client client = new Client(n_A, n_B, IP_A, IP_B);
        client.init("A", client.reader_A, client.writer_A);
        //client.init("B", client.reader_B, client.writer_B);

    }

    /**
     * @param n_a number of files to ask sequentially from server A
     * @param n_b number of files to ask sequentially from server B
     * @param ip_a IP address of server A
     * @param ip_b IP address of server B
     */
    public Client(int n_a, int n_b, String ip_a, String ip_b) {
        try {
            //initialize socket for server_A
            this.n_A = n_a;
            this.IP_A = ip_a;
            this.socket_A = new Socket(ip_a, PORT);
            writer_A = new ObjectOutputStream(socket_A.getOutputStream());
            reader_A = new ObjectInputStream(socket_A.getInputStream());

            //initialize socket for server_B
            this.n_B = n_b;
            this.IP_B = ip_b;
            /**
            socket_B = new Socket(ip_b, PORT);
            writer_B = new ObjectOutputStream(socket_B.getOutputStream());
            reader_B = new ObjectInputStream(socket_B.getInputStream());
             */

            serverToTime = new HashMap<Character, Double>();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void init(String s_id, ObjectInputStream reader, ObjectOutputStream writer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                double time_start = 0, time_end;
                try {
                    String first_msg = n_A + " " + n_B + " " + s_id;
                    writer.writeObject(first_msg);
                    time_start = System.currentTimeMillis();
                    //the server sends an empty MultiMediaFile object in order to cause an exception in this loop, and exit
                    while (true) {
                        MultiMediaFile file = (MultiMediaFile) reader.readObject();
                        System.out.println(file.getFileName());
                        Path new_file_path = Paths.get(pathOfFiles + file.getFileName());
                        //Files.write(new_file_path, file.getFileBuffer());
                    }

                } catch (ClassCastException e) {
                    time_end = (System.currentTimeMillis() - time_start)/1000.0;  //division returns seconds
                    serverToTime.put(s_id.charAt(0), time_end);
                    System.out.println("Finished receiving from server_" + s_id + " in " + time_end + " seconds");
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }).start();
    }
}
