import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class tcp_client {
    public static long getMin(ArrayList<Long> list){
        long min = list.get(0); // initialize min to the first element
        for (int i = 1; i < list.size(); i++) {
            long current = list.get(i);
            if (current < min) {
                // if the current element is smaller than min, update min
                min = current;
            }
        }

        return min;
    }
    public static long getMax(ArrayList<Long> list){
        long max = list.get(0); // initialize max to the first element
        for (int i = 1; i < list.size(); i++) {
            long current = list.get(i);
            if (current > max) {
                // if the current element is bigger than max, update max
                max = current;
            }
        }

        return max;
    }
    public static long getMean(ArrayList<Long> list){
        long sum = list.get(0); // initialize min to the first element
        for (int i = 1; i < list.size(); i++) {
            sum += list.get(i);
        }
        // Divides by number of elements for mean
        long mean = sum/list.size();
        return mean;
    }
    public static long getSD(ArrayList<Long> list){
        long mean = getMean(list);

        long sum = 0L;
        // initialize min to the first element
        for (int i = 0; i < list.size(); i++) {
            long dif = list.get(i)-mean;
            long DifSquared = dif*dif;
            // Sums all the differences squared
            sum += DifSquared;
        }
        // Gets variance
        long variance = sum/(list.size()-1);
        // sd = sqrt(variance)
        long sd = (long) Math.sqrt(variance);
        return sd;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            // Makes sure an argument was passed when running program
            System.out.println("Rerun program with hostname as argument");
        } else {
            // Sets up hostname based as argument for connection with server
            String hostName = args[0];
            int portNumber = 5201;
            // Sets up an array list to calculate statistics
            ArrayList<Long> clientGetMeme = new ArrayList<>();
            long totalTCPSetupTime = 0L;

            long setupTimeStart = System.nanoTime();
            try (
                    // Set up connection to server
                    Socket serverSocket = new Socket(hostName, portNumber);
                    // Set up ability to read from server
                    DataInputStream in = new DataInputStream(serverSocket.getInputStream());
                    // Sets up ability to write out to server
                    OutputStream outputStream = serverSocket.getOutputStream();
            ) {
                long setupTimeFinish = System.nanoTime();
                // Receive 10 joke files from server and write to local files
                for (int i = 1; i < 11; i++) {
                    try (
                            // Sets up the file to output jokes to
                            FileOutputStream fileOut = new FileOutputStream("joke" + i + ".jpg");
                    ) {
                        // Initializes buffer to send bytes of data over
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        // Sets start time for statistics
                        long startClientGetMeme = System.nanoTime();
                        while ((bytesRead = in.read(buffer)) != -1) {
                            if (bytesRead >= 2 && buffer[bytesRead - 2] == (byte) 0xFF && buffer[bytesRead - 1] == (byte) 0xFF) {
                                //breaks out of loop when end of transmission is signaled
                                break;
                            }
                            // Writes out the bytes if end of file not signalled
                            fileOut.write(buffer, 0, bytesRead);
                        }
                        // Timing for statistics
                        long finishClientGetMeme = System.nanoTime();
                        long timeElapsedGetMeme = (finishClientGetMeme - startClientGetMeme) / 1000000;
                        clientGetMeme.add(timeElapsedGetMeme);
                        // Lets server know ready for next file
                        outputStream.write("READY_FOR_NEXT_FILE".getBytes());

                        if (i == 10) {
                            // file 10 has been received so client knows all files have been successful
                            System.out.println("All jokes received and written to file successfully");
                        }
                        fileOut.flush();

                    }//Catch block
                    catch (IOException e) {
                        System.out.println("Error writing file: joke" + i + ".jpg");
                        System.out.println(e.getMessage());
                    }
                }
                totalTCPSetupTime = (setupTimeFinish - setupTimeStart) / 1000000;
                // Catch block
            } catch (IOException e) {
                System.out.println("Exception caught while connecting to server");
                System.out.println(e.getMessage());
            }
            // Prints out all the calculated statistics
            System.out.println("TCP setup time (ms): " + totalTCPSetupTime);
            System.out.println("Round trip time for each image min (ms): " + getMin(clientGetMeme));
            System.out.println("Round trip time for each image max (ms): " + getMax(clientGetMeme));
            System.out.println("Round trip time for each image mean (ms): " + getMean(clientGetMeme));
            System.out.println("Round trip time for each image standard deviation (ms): " + getSD(clientGetMeme));
        }
    }
}