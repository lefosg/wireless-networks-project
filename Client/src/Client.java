import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Client {
    private Socket socket_A, socket_B;
    private ObjectInputStream reader_A, reader_B;
    private ObjectOutputStream writer_A, writer_B;
    private int PORT_A, PORT_B;
    private String IP_A, IP_B;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Not enough parameters specified");
            System.exit(0);
        }

        int n_A = Integer.parseInt(args[0]);
        int n_B = Integer.parseInt(args[1]);
        String IP_A = args[2];
        String IP_B = args[3];

        System.out.println(n_A);
        System.out.println(n_B);
        System.out.println(IP_A);
        System.out.println(IP_B);

    }

    public Client(String ip_a, int port_a, String ip_b, int port_b) {
        try {
            //initialize socket for server_A
            this.PORT_A = port_a;
            this.IP_A = ip_a;
            socket_A = new Socket(ip_a, port_a);

            //initialize socket for server_B
            /**
            this.PORT_B = port_b;
            this.IP_B = ip_b;
            socket_B = new Socket(ip_b, port_b);
             */
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
