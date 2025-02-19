import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;

class TCPServer {
   
    public static DatagramPacket tossJoke(String jokeInput, InetAddress address, int port) {
        try {
            //Toss joke now only returns a proper Datagram packet rather than the entire necesary data.
            System.out.println("Accessing image...");
            /* =========================================================
             * ===========MEASURING UDP LOCAL ACESS TIME    ============
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
            DatagramPacket packet = new DatagramPacket(buffer.toByteArray(), buffer.size(), address, port);
            return packet;
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } 
        catch(StringIndexOutOfBoundsException e) {
            System.out.println("File not found");
            return null;
        } 
        catch(Exception e) {
            System.out.println("Error Tossing Joke: " + e);
            return null;
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
            /* =========================================================
             * ===========MEASURING UDP ADDRESS RESOLUTION TIME=========
             * =========================================================
            */
            long start = System.nanoTime();
            System.out.println("Recieving request from client...");
            //The UDP case is much simpler, we just create Datagrams and use our previous functions to pass them to the client.
            DatagramSocket server = new DatagramSocket(Integer.parseInt(args[0]));
            //We first recieve a simple message of engagement from the client which we use to get address and port.
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            server.receive(packet);
            System.out.println("Request recieved. Sending information...");
            InetAddress address = packet.getAddress();
            long end = System.nanoTime();
            System.out.println("Address Resolved in " + (end - start) + "ns\n");
            int port = packet.getPort();
            ArrayList<Integer> randImgs = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));
            Collections.shuffle(randImgs);
            ArrayList<Long> timesList = new ArrayList<Long>();
            /* =========================================================
             * ===========MEASURING UDP TRANSMISSION TIMES     =========
             * =========================================================
            */
            long longStart = System.nanoTime();
            for(int i = 0; i < 10; i++) {
                start = System.nanoTime();
                System.out.println("Now sending: joke" + i + ".jpg...");
                //Got the client request, now its time to send over two info. First we send the image size, then the image itself. 
                DatagramPacket imageGotten = tossJoke(Integer.toString(randImgs.get(i)), address, port); 
                ByteBuffer b = ByteBuffer.allocate(4);
                b.putInt(imageGotten.getLength());
                buf = b.array();
                DatagramPacket imageSize = new DatagramPacket(buf, buf.length, address, port);
                server.send(imageSize);
                System.out.println("Image Size Sent Sucessfully");
                server.send(imageGotten);
                System.out.println("Image sent sucessfully");
                end = System.nanoTime();
                timesList.add(end - start);
            }
            long longEnd = System.nanoTime();
            getPingMessage(timesList, longStart, longEnd);
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
