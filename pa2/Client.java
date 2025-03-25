import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private Socket s = null;
    private DataInputStream tin = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    public Client(String addr, int port) {
        try {
            s = new Socket(addr, port);
            System.out.println("Connected to server");

            tin = new DataInputStream(System.in);
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            System.out.println(in.readUTF());

            List<Long> rttTimes = new ArrayList<>();

            while (true) {
                System.out.print("Enter file name (or 'bye' to quit): ");
                String fileName = tin.readLine();

                long startTime = System.currentTimeMillis();
                out.writeUTF(fileName);

                if (fileName.equals("bye")) {
                    System.out.println(in.readUTF());
                    break;
                }

                try {
                    String serverResponse = in.readUTF();
                    if (serverResponse.equals("File not found")) {
                        System.out.println("File not found on server.");
                        continue;
                    }

                    long fileSize = in.readLong();
                    String savePath = "received_" + fileName;
                    receiveFile(savePath, fileSize);

                    long endTime = System.currentTimeMillis();
                    long rtt = endTime - startTime;
                    rttTimes.add(rtt);
                    System.out.println("File received. RTT: " + rtt + " ms");

                } catch (IOException i) {
                    System.out.println("Error receiving file: " + i.getMessage());
                }
            }

            if (!rttTimes.isEmpty()) {
                calculateAndPrintStatistics(rttTimes);
            }

        } catch (IOException i) {
            System.out.println("Error: " + i.getMessage());
        } finally {
            try {
                if (tin != null) tin.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (s != null) s.close();
            } catch (IOException i) {
                System.out.println("Error closing connections: " + i.getMessage());
            }
        }
    }

    private void receiveFile(String savePath, long fileSize) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(savePath);
        byte[] buffer = new byte[4096];
        long bytesReceived = 0;

        while (bytesReceived < fileSize) {
            int bytesToRead = (int) Math.min(buffer.length, fileSize - bytesReceived);
            int bytesRead = in.read(buffer, 0, bytesToRead);
            if (bytesRead == -1) break;

            fileOut.write(buffer, 0, bytesRead);
            bytesReceived += bytesRead;
        }

        fileOut.close();
        System.out.println("File saved: " + savePath);
    }

    private void calculateAndPrintStatistics(List<Long> rttTimes) {
        long min = rttTimes.get(0), max = rttTimes.get(0);
        double sum = rttTimes.get(0);

        for (int i = 1; i < rttTimes.size(); i++) {
            long time = rttTimes.get(i);
            if (time < min) min = time;
            if (time > max) max = time;
            sum += time;
        }

        double mean = sum / rttTimes.size();
        double stdDevSum = 0;
        for (long time : rttTimes) {
            stdDevSum += Math.pow(time - mean, 2);
        }
        double stdDev = Math.sqrt(stdDevSum / rttTimes.size());

        System.out.println("\n--- RTT Statistics ---");
        System.out.println("Min RTT: " + min + " ms");
        System.out.println("Max RTT: " + max + " ms");
        System.out.println("Mean RTT: " + mean + " ms");
        System.out.println("Standard Deviation: " + stdDev + " ms");
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 1339;

        if (args.length >= 2) {
            serverAddress = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port. Using default: 1339");
            }
        }

        new Client(serverAddress, port);
    }
}
