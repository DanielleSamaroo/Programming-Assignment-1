import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class client {

    // Functions to get the necessary statistics
    public static long getMin(ArrayList<Long> list) {
        long min = list.get(0); 
        for (int i = 1; i < list.size(); i++) {
            long current = list.get(i);
            if (current < min) {
                
                min = current;
            }
        }
        return min;
    }

    public static long getMax(ArrayList<Long> list) {
        long max = list.get(0); 
        for (int i = 1; i < list.size(); i++) {
            long current = list.get(i);
            if (current > max) {
                
                max = current;
            }
        }
        return max;
    }

    public static long getMean(ArrayList<Long> list) {
        long sum = 0L;
        for (long value : list) {
            sum += value;
        }
        return sum / list.size();
    }

    public static long getSD(ArrayList<Long> list) {
        long mean = getMean(list);
        long sum = 0L;
        for (long value : list) {
            long diff = value - mean;
            sum += diff * diff;
        }
        long variance = sum / (list.size() - 1);
        return (long) Math.sqrt(variance);
    }

    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.out.println("Run with java client [server_machine] [port_number]");
            return;
        }
        
        String serverURL = args[0];
        int portNumber = 5201; // Just set a default port number for the program in case user does not specify
        try {
            portNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number provided. Using default port 5201.");
        }
        
        ArrayList<Long> roundTripTimes = new ArrayList<>();
       
        
        try {
            
            // Initial connection to the server
            Socket socket = new Socket(serverURL, portNumber);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
           
            
            
            String greeting = in.readUTF();
            System.out.println("Server: " + greeting);
            
            
            Scanner scanner = new Scanner(System.in);
            boolean running = true;
            
            while (running) {
                System.out.print("Enter file name (type 'bye' to quit): ");
                String fileName = scanner.nextLine();
                
                // Start timer before sending file name.
                long startTime = System.nanoTime();
                out.writeUTF(fileName);
                out.flush();
                
                // If user wants to quit.
                if (fileName.equalsIgnoreCase("bye")) {
                    String disconnectMessage = in.readUTF();
                    if (disconnectMessage.equals("disconnected")) {
                        System.out.println("Server: " + disconnectMessage);
                        System.out.println("Exiting.");
                    }
                    running = false;
                    break;
                }
                
                
                String response = in.readUTF();
                if (response.equals("File not found") || response.startsWith("Exception:")) {
            
                    System.out.println("Server: " + response);
                    continue;
                }
                // Found is the indicator from the server that the file was found and is about to be sent
                if (response.equals("FOUND")) {
                    
                    long fileSize = in.readLong();
                    File dir = new File("received_files");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    // Save the file inside the "received_files" folder with the original name
                    File receivedFile = new File(dir, fileName);
                    FileOutputStream fileOut = new FileOutputStream(receivedFile);
                    
                    
                    byte[] buffer = new byte[1024];
                    long remaining = fileSize;
                    // Makes sure all of the file is read in
                    while (remaining > 0) {
                        int bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (bytesRead == -1) {
                            break;
                        }
                        fileOut.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }
                    fileOut.close();
                    
                    
                    long finishTime = System.nanoTime();
                    long roundTripTime = (finishTime - startTime) / 1000000;
                    roundTripTimes.add(roundTripTime);
                    System.out.println("File " + fileName + " received and saved as " 
                                       + receivedFile.getName() + " (Round-trip time: " 
                                       + roundTripTime + " ms)");
                }
            }
            
            scanner.close();
            in.close();
            out.close();
            socket.close();
            
            if (!roundTripTimes.isEmpty()) {
                System.out.println("Round trip time min (ms): " + getMin(roundTripTimes));
                System.out.println("Round trip time max (ms): " + getMax(roundTripTimes));
                System.out.println("Round trip time mean (ms): " + getMean(roundTripTimes));
                System.out.println("Round trip time standard deviation (ms): " + getSD(roundTripTimes));
            }
            
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
