// Demonstrating Server-side Programming
import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    // Initialize socket and input stream
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    // Constructor with port
    public Server(int port) {

        // Starts server and waits for a connection
        try
        {
            ss = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            // Takes input from the client socket
            in = new DataInputStream(
                    new BufferedInputStream(s.getInputStream()));

            out = new DataOutputStream(s.getOutputStream());

            String clientOutput = "";
            try{
                out.writeUTF("Hello!");
            } catch (IOException i){
                System.out.println(i);
            }
            // Reads message from client until "Over" is sent
            while (!clientOutput.equals("bye"))
            {
                try
                {
                    clientOutput = in.readUTF();
                    System.out.println(clientOutput);
                    if(clientOutput.matches("[a-zA-Z ]+") && !clientOutput.equals("bye")){
                        clientOutput=clientOutput.toUpperCase();
                        out.writeUTF(clientOutput);
                    }else if(!clientOutput.equals("bye")){
                        out.writeUTF("Illegal string of characters, please retransmit");
                    }

                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            out.writeUTF("disconnected");

            // Close connection
            s.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[])
    {
        Server s = new Server(1339);
    }
}
