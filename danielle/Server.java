import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    out.println("Hello!");
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.equals("bye")) {
                            out.println("disconnected");
                            break;
                        }

                        if (inputLine.matches("[a-zA-Z]+")) {
                            out.println(inputLine.toUpperCase());
                        } else {
                            out.println("Invalid input, please resend.");
                        }
                    }
                } finally {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
