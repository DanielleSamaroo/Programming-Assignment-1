import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(in.readLine());

            String userInput;
            while (true) {
                System.out.print("Enter a string: ");
                userInput = scanner.nextLine();

                long startTime = System.nanoTime();
                out.println(userInput);
                String response = in.readLine();
                long endTime = System.nanoTime();

                long roundTripTime = (endTime - startTime) / 1_000_000;
                System.out.println("Server response: " + response);
                System.out.println("Round-trip time: " + roundTripTime + " ms");

                if (response.equals("disconnected")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
