import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;

class server {
   
    public static String tossJoke(String jokeInput, DataOutputStream out, DataInputStream in) {
        try {
            //Opens the file 
            System.out.println("Accessing image...");
            /* =========================================================
             * ===========MEASURING TCP LOCAL ACESS TIME    ============
             * =========================================================
            */
            long start = System.nanoTime();
            BufferedImage file = ImageIO.read(new File("joke" + jokeInput +".jpg")); 
            long end = System.nanoTime();
            System.out.println("Time taken to access joke" + jokeInput + ".jpg: " + (end - start) + "ns");
            //Loads joke file into buffer and sends it over to the client.
            //Most of these adaptations for images were taken from several places including:
            //https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
            //https://alvinalexander.com/blog/post/java/open-read-image-file-java-imageio-class/
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); 
            ImageIO.write(file, "jpg", buffer);
            byte[] size = ByteBuffer.allocate(4).putInt(buffer.size()).array();
            out.write(size);
            out.write(buffer.toByteArray());
            out.flush();
            return "";
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found");
            return "error";
        } 
        catch(StringIndexOutOfBoundsException e) {
            System.out.println("File not found");
            return "error";
        } 
        catch(Exception e) {
            System.out.println("Error Tossing Joke: " + e);
            return "error";
        }
    }

    public static void getPingMessage(ArrayList<Long> timesList, long longStart, long longEnd) {
        System.out.println("\n\n--- ping statistics --");
        System.out.println(timesList.size() + " images transmitted, " + timesList.size() + " recieved, " + (100.0 - (10.0/timesList.size()) * 100) + "% loss, time " + (longEnd - longStart) + "ms");
        long average = 0;
        for(int i = 0; i < timesList.size(); i++) {
            average += timesList.get(i);
        }
        average /= timesList.size();
        double sd = 0;
        for(int  i = 0; i < timesList.size(); i++) {
            sd += Math.pow((timesList.get(i) - average), 2);
        }
        sd /= timesList.size();
        sd = Math.sqrt(sd);
        System.out.print("rtt min/avg/max/mdev = ");
        System.out.print(Collections.min(timesList) + "/" + average + "/" + Collections.max(timesList) + "/" + (long)sd + " ms");
    }
    
    public static void main(String[] args) {
        try {
            //ServerSocket provides serverside functionality for Java. Argument passed in creating the server is the port number.
            //As per project instructions, recommended to use last 4 digits of UFID since there are a lot of reserved ports
            ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println("Spinning up server, waiting for client connection...");

            //Wait for the client request. getInetAddress 
            Socket client = server.accept();
            System.out.println("Got connection request from: " + client.getInetAddress().toString());

            //Out is used to send information to the client
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            //outFile and inFile are used to send entire files to the client
            DataOutputStream outFile = new DataOutputStream(client.getOutputStream());
            DataInputStream inFile = new DataInputStream(client.getInputStream()); 
            out.println("Hello!");

            //In is used to take in data from the client. Exception handeling comes from this main while loop.
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            ArrayList<Integer> randImgs = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
            Collections.shuffle(randImgs);
            /* =========================================================
             * ===========MEASURING TCP TOTAL SEND TIME    =============
             * =========================================================
            */
            ArrayList<Long> timesList = new ArrayList<Long>();
            long longStart = System.nanoTime();
            for(int i = 0; i < 10; i++) {
                System.out.println("\nGetting ready to toss image " + randImgs.get(i) + "...");
                long start = System.nanoTime();
                tossJoke(Integer.toString(randImgs.get(i)), outFile, inFile);
                long end = System.nanoTime();
                timesList.add(end - start);
            }
            long longEnd = System.nanoTime();
            getPingMessage(timesList, longStart, longEnd);
            in.close();
            out.println("Client Disconnected");
            out.close();
            server.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}


/*
 * Resources:
 * https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/socketprogramming/socketprogram.html: For essentially giving a guide for this entire project
 * https://www.geeksforgeeks.org/java-net-serversocket-class-in-java/: For understanding the serverSocket class
 * https://stackoverflow.com/questions/5757900/gethostaddress-and-getinetaddress-in-java: For getting address for client/server
 * https://www.geeksforgeeks.org/transfer-the-file-client-socket-to-server-socket-in-java/: Mostly for handling files in Java
 */
