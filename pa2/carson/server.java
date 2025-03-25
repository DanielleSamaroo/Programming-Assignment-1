import java.io.*;
import java.net.*;

public class server {
    
    // Set the folder path where the bmp_files are actually stored 
    private static final String FILE_DIR = "bmp_files"; 

    public static void main(String[] args) {
        // Setting a default port number in case it is not specified
        int portNumber = 5201; 

        if (args.length > 0) {
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number provided. Using default port 5201.");
            }
        }
        
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server started on port " + portNumber + ". Waiting for client connection...");
            
            
            clientSocket = serverSocket.accept();
            System.out.println("Client connected from " + clientSocket.getInetAddress());
            
            
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            
            // Sends initial message to client after connection was established
            out.writeUTF("Hello!");
            out.flush();
            
            boolean running = true;
            while (running) {
                
                String fileName = in.readUTF();
                System.out.println("Received file request: " + fileName);
                
                // If client sends the disconnect message
                if (fileName.equalsIgnoreCase("bye")) {
                    out.writeUTF("disconnected");
                    out.flush();
                    running = false;
                    System.out.println("Client requested termination. Closing connection.");
                    break;
                }
                
                File file = new File(FILE_DIR, fileName);
                
                if (!file.exists()) {
                    // This handles if the file does not exist
                    out.writeUTF("File not found");
                    out.flush();
                    System.out.println("File " + file.getAbsolutePath() + " not found. Informed client.");
                } else {
                    try {
                        // Send a header message to client to let them know the file does exist
                        // This lets the client know to expect the file size in bytes as the next message
                        out.writeUTF("FOUND");
                        // Send the file size so the client knows how many bytes to expect
                        out.writeLong(file.length());
                        
                        // Sends the actual file content
                        FileInputStream fileIn = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileIn.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        fileIn.close();
                        out.flush();
                        System.out.println("File " + file.getName() + " sent successfully.");
                    } catch (IOException e) {
                        // If an exception occurs while sending the file, send the exception message to the client.
                        out.writeUTF("Exception: " + e.getMessage());
                        out.flush();
                        System.out.println("Exception while sending file: " + e.getMessage());
                    }
                }
            }
            
            
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            System.out.println("Server terminated gracefully.");
            
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
        }
    }
}
