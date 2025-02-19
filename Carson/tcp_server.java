import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class tcp_server {
    public static long getMin(ArrayList<Long> list){
        long min = list.get(0); // initialize min to the first element
        for (int i = 1; i < list.size(); i++) {
            long current = list.get(i);
            if (current < min) {
                // if the current element is smaller than min sets min to current element
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
                // if the current element is bigger than max sets max to current element
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
        int portNumber = 5201;
        List<String> jokesList = new ArrayList<String>();
        ArrayList<Long> serverGetFile = new ArrayList<>();
        byte[] buffer = new byte[1024];

        // Adds the jokes to a list to be randomly pulled from
        jokesList.add("joke1.jpg");
        jokesList.add("joke2.jpg");
        jokesList.add("joke3.jpg");
        jokesList.add("joke4.jpg");
        jokesList.add("joke5.jpg");
        jokesList.add("joke6.jpg");
        jokesList.add("joke7.jpg");
        jokesList.add("joke8.jpg");
        jokesList.add("joke9.jpg");
        jokesList.add("joke10.jpg");
        System.out.println("Waiting for client to connect...");
        try (
                // Sets up socket and awaits connection from client
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                // Sets up ability to write to client from server
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                InputStream inputStream = clientSocket.getInputStream();
                ) {
            System.out.println("Client connected");

            Random rand = new Random();
            for (int i = 1; i < 11; i++) {
                // sets randInd to random integer within range of jokesList size
                int randInd = rand.nextInt(jokesList.size());
                // Sets jokeFile equal to the name of the joke at that int
                String jokeFile = jokesList.get(randInd);
                // Removes that joke from the list
                jokesList.remove(randInd);
                // Starts timer
                long memeFileAccessStart = System.nanoTime();
                try (
                        // Sets up file input stream
                        FileInputStream fileIn = new FileInputStream(jokeFile);
                ) {
                    System.out.println("Sending " + jokeFile + " to client...");
                    // Sends joke file to client
                    int bytesRead;
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                    //    System.out.println("While loop hit");
                        out.write(buffer, 0, bytesRead);
                    }
                    // Ends timer
                    long memeFileAccessFinish = System.nanoTime();
                    // Writes out byte data to indicate end of file
                    out.write(new byte[]{(byte) 0xFF, (byte) 0xFF});
                    long memeFileAccessTotal = (memeFileAccessFinish - memeFileAccessStart)/1000000;
                    serverGetFile.add(memeFileAccessTotal);

                    // Resets the file in reading position to start of the file
                    fileIn.getChannel().position(0);

                    // Set up to read message from client notifying server that client is
                    // ready for next file
                    byte[] bufferNew = new byte[1024];
                    int bytesReadNew = inputStream.read(bufferNew);
                    String message = new String(bufferNew, 0, bytesReadNew);
                    if (message.equals("READY_FOR_NEXT_FILE")) {
                        //Prints out to console and sends next file
                        System.out.println("Client ready for next meme");
                    }

                } catch (FileNotFoundException e) {
                    // Catch block if cannot find file
                    System.out.println("Error: joke file not found");
                    out.writeUTF("Error: joke file not found");
                }
            }
        } catch (IOException e) {
            // Catch block if connection issue
            System.out.println("Exception caught while waiting on port " + portNumber);
            System.out.println(e.getMessage());
        }
        // Prints out all the statistics
        System.out.println("Time to read file contents into byte array min (ms): " + getMin(serverGetFile));
        System.out.println("Time to read file contents into byte array max (ms): " + getMax(serverGetFile));
        System.out.println("Time to read file contents into byte array mean (ms): " + getMean(serverGetFile));
        System.out.println("Time to read file contents into byte array standard deviation (ms): " + getSD(serverGetFile));

    }
}