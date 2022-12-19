package match_the_word;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    Scanner scan;
    Socket socket = null;

    DataOutputStream output = null;
    DataInputStream input = null;

    InetAddress ip;

    public Client() {
        try {
            ip = InetAddress.getByName("localhost");
            socket = new Socket(ip, Constants.PORT);

            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            scan = new Scanner(System.in);
        } catch (UnknownHostException ex) {
            log("CLIENT ERROR => " + ex.getMessage());
        } catch (IOException ex) {
            log("CLIENT ERROR => " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.writeMessageThread();
        client.readMessageThread();
    }
    
    private void writeMessageThread() {
        Thread SendMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String message = scan.nextLine();

                    try {
                        output.writeUTF(message);
                    } catch (IOException ex) {
                        log("WRITE MESSAGE THREAD ERROR => " + ex.getMessage());
                    }
                }
            }
        });
        SendMessageThread.start();
    }
    
    private void readMessageThread() {
        Thread ReadMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String message = input.readUTF();
                        log(message);
                    } catch (IOException ex) {
                        log("READ MESSAGE THREAD ERROR => " + ex.getMessage());
                    }
                }
            }
        });
        ReadMessageThread.start();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
