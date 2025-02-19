import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import javax.imageio.ImageIO;

class UDPClient {

    public static String catchJoke(DatagramPacket packet, int num) {
        try {
            byte[] imageBuf = new byte[packet.getLength()];
            imageBuf = packet.getData();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBuf));
            ImageIO.write(image, "jpg", new File("joke" + num + ".jpg"));
            return "";
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found");
            return "error";
        } 
        catch(EOFException e) {
            // : ( this doesn't work nice, so now it just always calls this on exit. Bad practice.
            return "error";
        }
        catch(Exception e) {
            System.out.println("Error Catching Joke: " + e);
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
            //Send a packet to the server so it can get our address
            System.out.println("Sending request to server...");
            DatagramSocket client = new DatagramSocket();
            byte[] buf = "Address?".getBytes();
            InetAddress address = InetAddress.getByName(args[0]);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Integer.parseInt(args[1]));
            client.send(packet);
            //After sending the packet of our details, we then recieve the length of the incoming data
            System.out.println("Request sent. Recieving data...");
            /* =========================================================
             * ===========MEASURING UDP RECIEPT TIMES          =========
             * =========================================================
            */
            ArrayList<Long> timesList = new ArrayList<Long>();
            long longStart = System.nanoTime();
            for(int i = 0; i < 10; i++) {
                long start = System.nanoTime();
                buf = new byte[8];
                DatagramPacket imageSize = new DatagramPacket(buf, buf.length, address, Integer.parseInt(args[1]));
                client.receive(imageSize);
                //Finally we catch the incoming data and use it
                System.out.println("Recieved image size. Now retrieving image data...");
                ByteBuffer wrapped = ByteBuffer.wrap(imageSize.getData()); 
                int size = wrapped.getInt();
                buf = new byte[size];
                DatagramPacket recievedImage = new DatagramPacket(buf, buf.length, address, Integer.parseInt(args[1]));
                client.receive(recievedImage);
                catchJoke(recievedImage, i);
                long end = System.nanoTime();
                System.out.println("Recieved Image " + i + " in " + (end - start) + "ns");
                timesList.add(end - start);
            }
            long longEnd = System.nanoTime();
            getPingMessage(timesList, longStart, longEnd);
            client.close();
        } 
        catch (Exception e) {
            System.out.println(e);
        }
    }
}