import java.net.*;
import java.io.*;

public class Server {

    private ServerSocket ss = null;
    private Socket s = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private final String fileDirectory = "..\\BMP_Files\\";

    public Server(int port) {
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started, waiting for client...");

            s = ss.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = new DataOutputStream(s.getOutputStream());

            out.writeUTF("Hello!");

            String clientOutput = "";

            while (!clientOutput.equals("bye")) {
                try {
                    clientOutput = in.readUTF();
                    System.out.println("Client requested: " + clientOutput);

                    if (clientOutput.equals("bye")) {
                        out.writeUTF("disconnected");
                    } else {
                        sendFile(clientOutput);
                    }

                } catch (IOException i) {
                    System.out.println("Error: " + i.getMessage());
                }
            }

            System.out.println("Closing connection...");
            s.close();
            in.close();
            out.close();
            ss.close();

        } catch (IOException i) {
            System.out.println("Error: " + i.getMessage());
        }
    }

    private void sendFile(String fileName) {
        String filePath = fileDirectory + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            try {
                long fileSize = file.length();
                out.writeUTF("File exists");
                out.writeLong(fileSize);

                FileInputStream fileIn = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                fileIn.close();
                System.out.println("File sent: " + fileName);

            } catch (IOException i) {
                System.out.println("Error sending file: " + i.getMessage());
            }
        } else {
            try {
                out.writeUTF("File not found");
                System.out.println("File not found: " + fileName);
            } catch (IOException i) {
                System.out.println("Error sending 'File not found' message: " + i.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        int port = 1339;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: 1339");
            }
        }
        new Server(port);
    }
}
