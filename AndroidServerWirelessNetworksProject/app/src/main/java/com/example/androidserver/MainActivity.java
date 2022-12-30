package com.example.androidserver;

import com.wirelessnetworks.multimediafile.MultiMediaFile;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ServerSocket serverSocket;
    private String ip;
    private int PORT = 7000;
    private int numberOfFiles;
    private final String pathOfFiles ="project-files/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * Handler for the button on the screen. Starts-Stops the server.
     * @param view
     */
    @SuppressLint("SetTextI18n")
    public void startAndroidServer(View view) {
        Button button = findViewById(R.id.start_server_button);

        if (button.getText().equals("START SERVER")) {
            new InitServer().execute();
            button.setText("STOP SERVER");
            Toast.makeText(MainActivity.this, "Server started", Toast.LENGTH_SHORT).show();
        }
        else if (button.getText().equals("STOP SERVER")) {
            serverSocket = null;
            button.setText("START SERVER");
            Toast.makeText(MainActivity.this, "Server stopped", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(MainActivity.this, "Cannot start server", Toast.LENGTH_SHORT).show();
        }
    }

    private class InitServer extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {
            try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                serverSocket = findAvailableServerSocket();
                datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = datagramSocket.getLocalAddress().getHostAddress();
                System.out.println("AndroidServer is running on " + ip + ":" + PORT);

                numberOfFiles = getNumberOfFiles();

                init();
            } catch (Exception e) {
                System.err.println(e);
                Button button = findViewById(R.id.start_server_button);
                button.setText("START SERVER");
                Toast.makeText(MainActivity.this, "ERROR - Server stopped running", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    /**
     * A method to create a {@code ServerSocket} automatically, detecting whether the PORT is used. If PORT is in use
     * it increments it and tries the next one. <br>
     * Note: this method is meaningful only when there will be two servers on the same machine.
     * @return a {@code ServerSocket}
     */
    public ServerSocket findAvailableServerSocket() {
        while (true) {
            try {
                ServerSocket s = new ServerSocket(this.PORT);
                return s;
            } catch (Exception e) {
                this.PORT ++;
            }
        }
    }

    /**
     *
     * @return number of files existing in the {@link #pathOfFiles}
     */
    public int getNumberOfFiles() {
        try {
            return getAssets().list(pathOfFiles).length;
        } catch (Exception e) {
            System.err.println(e);
            return -1;
        }
    }

    /**
     * Calculate the names of the files to be sent to the client.
     * Example: if id="A" and n_a=2 and n_b=3, this server acts as server_A must send the first 2 files <b>(s001.m4s, s002.ms4)</b>
     * server_B the next 3 (s003.m4s, s004.ms4, s005.ms4), then this server sends the next 2 <b>(s006.ms4, s007.ms4)</b> etc.
     * We get all these names and add them in an {@code ArrayList}, then return it.
     * @param n_a files requested from server_A
     * @param n_b files requested from server_B
     * @param id the id of the server, "A" or "B"
     * @return an {@code ArrayList} of  the file names to be sent
     */
    public ArrayList<String> calculateFilesToSend(int n_a, int n_b, String id) {
        ArrayList<String> file_names = new ArrayList<String>();

        if (id.equals("A")) {
            //if it's server_A it sends the first files n_a files
            for (int i = 1; i <= numberOfFiles; i+=n_b+n_a) {
                for (int j = i; j < i+n_a; j++) {
                    String index = j < 100 ? j < 10? "00"+(j) : "0"+(j) : String.valueOf(j);
                    file_names.add("s" + index + ".m4s");
                    System.out.println("s" + index + ".m4s");
                }
            }
        } else if (id.equals("B")) {
            //if it's server_B it means server_A sends the first n_a files, so server_B must calculate the rest
            for (int i = n_a+1; i <= numberOfFiles; i+=n_b+n_a) {
                for (int j = i; j < i+n_b; j++) {
                    String index = j < 100 ? j < 10? "00"+(j) : "0"+(j) : String.valueOf(j);
                    file_names.add("s" + index + ".m4s");
                    System.out.println("s" + index + ".m4s");
                }
            }

        } else {
            System.err.println("Unknown id for server");
            return null;
        }

        return file_names;
    }

    public void init() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());

                /**
                 * The first thing the client sends is the String "n_X X", where X='A' or X='B'.
                 * We send X to notify to the server the order of sending the files. After the server understands its identity
                 * it can calculate which files are needed to send, so they don't overlap with server_B's.
                 * The client sends this message to both servers when initiated
                 */
                //parse client info
                String[] client_info = ((String) reader.readObject()).split(" ");
                int n_A = Integer.parseInt(client_info[0]);
                int n_B = Integer.parseInt(client_info[1]);
                String server_id = client_info[2];
                System.out.println("--- Client information ---\nn_A: " + n_A + "\nn_B: " + n_B + "\nAndroidServer id: " + server_id);
                //get names of files to send, e.g. "s001.m4s", "s050.m4s" ...
                ArrayList<String> file_names = calculateFilesToSend(n_A, n_B, server_id);
                if (file_names == null) {
                    System.err.println("File names to send are null, disconnecting from client..");
                    continue;
                }
                //transmit the files
                for (String fileName : file_names) {
                    //get contents of file
                    DataInputStream dataInputStream;
                    InputStream inputStream = getAssets().open(pathOfFiles + fileName);
                    dataInputStream = new DataInputStream(inputStream);
                    int size = dataInputStream.available();
                    byte[] file_bytes = new byte[size];
                    dataInputStream.readFully(file_bytes);

                    dataInputStream.close();
                    inputStream.close();

                    //create the MultiMediaFile object and send it
                    MultiMediaFile mediaFile = new MultiMediaFile(fileName, file_bytes);
                    writer.writeObject(mediaFile);
                }
                //on transmission end, send an empty string to signal the end
                writer.writeObject(new String(""));
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }

}