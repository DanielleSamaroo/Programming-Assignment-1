// Demonstrating Client-side Programming
import java.io.*;
import java.net.*;

public class Client {

    // Initialize socket and input/output streams
    private Socket s = null;
    private DataInputStream tin = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    // Constructor to put IP address and port
    public Client(String addr, int port)
    {
        // Establish a connection
        try {
            s = new Socket(addr, port);
            System.out.println("Connected");

            // Takes input from terminal
            tin = new DataInputStream(System.in);

            // Sends output to the socket
            out = new DataOutputStream(s.getOutputStream());

            //receives input from socket
            in = new DataInputStream(
                    new BufferedInputStream(s.getInputStream()));
        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }

        // String to read message from input
        String terminalInput = "";
        String serverOutput = "";
        try {
            serverOutput = in.readUTF();
            System.out.println(serverOutput);
        }catch (IOException i) {
            System.out.println(i);
        }
        // Keep reading until "disconnected" is received
        while (!serverOutput.equals("disconnected")) {
            try {
                terminalInput = tin.readLine();
                if(!terminalInput.isEmpty()){
                    long startTime = System.currentTimeMillis();
                    out.writeUTF(terminalInput);
                    serverOutput = in.readUTF();
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    System.out.println(serverOutput);
                    if(!serverOutput.equals("disconnected")) {
                        System.out.println(elapsedTime);
                    }
                }
            }
            catch (IOException i) {
                System.out.println(i);
            }
        }

        // Close the connection
        try {
            tin.close();
            out.close();
            in.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        Client c = new Client("localhost", 1339);
    }
}
