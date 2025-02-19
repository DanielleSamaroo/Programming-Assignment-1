import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;

class client {

    public static String catchJoke(DataInputStream in, DataOutputStream out, String input, int num) {
        try {
            if(input == "bye") {
                return input;
            }
            byte[] sizeBuf = new byte[4];
            in.read(sizeBuf);
            int size = ByteBuffer.wrap(sizeBuf).asIntBuffer().get();
            byte[] imageBuf = new byte[size];
            in.read(imageBuf);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBuf));
            ImageIO.write(image, "jpg", new File("joke" + num + ".png"));
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
            //Refer to server for implementation details and explanations of these functions. THe same logic mostly applies for this section.
            //In this case, args[0] is the hostName and args[1] is the port number.
            System.out.println("Establishing connection...");
            /* =========================================================
             * ===========MEASURING TCP HANDSHAKE TIME==================
             * =========================================================
            */
            long start = System.nanoTime();
            Socket client = new Socket(args[0], Integer.parseInt(args[1]));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())); 
            DataInputStream fileIn = new DataInputStream(client.getInputStream());
            DataOutputStream fileOut = new DataOutputStream(client.getOutputStream());
            long end = System.nanoTime();
            System.out.println("Connection established. Time taken: " + (end - start) + "ns");
            System.out.println("Recieved message from " + client.getInetAddress().toString() + ": " + in.readLine());
            Scanner scanner = new Scanner(System.in);
            String input = "";
            /* =========================================================
             * ===========MEASURING TCP TOTAL SEND TIME    =============
             * =========================================================
            */
            System.out.println("Awaiting Images...\n");
            long longStart = System.nanoTime();
            ArrayList<Long> timesList = new ArrayList<Long>();
            for(int i = 0; i < 10; i++) {
                start = System.nanoTime();
                if(catchJoke(fileIn, fileOut, input, i) == "error") {
                    System.out.println("Failed to recieve image");
                }
                else {
                    end = System.nanoTime();
                    System.out.println("Recieved image " + (i+1) + "/10");
                    System.out.println("Time taken: " + (end - start) + "ns\n");
                    timesList.add(end - start);
                }
            }
            long longEnd = System.nanoTime();
            //Printing out the ping statistics
            getPingMessage(timesList, longStart, longEnd);
            
            out.println("bye");
            //Needs work on making proper goodbye call 
            //String goodbye = in.readLine();
            //System.out.println(goodbye);
            in.close();
            out.close();
            scanner.close();
            client.close();
        } 
        catch (Exception e) {
            System.out.println(e);
        }
    }
}
