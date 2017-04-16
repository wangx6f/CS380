import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by xinyuan_wang on 4/7/17.
 */
public class Client {
     public static void main(String[] args) throws IOException,InterruptedException
     {

         Socket socket = new Socket("codebank.xyz",38001);
         String input;
         OutputStream outStream = socket.getOutputStream();
         PrintStream printStream = new PrintStream(outStream);
         InputStream inStream = socket.getInputStream();
         InputStreamReader inStreamReader = new InputStreamReader(inStream);
         BufferedReader bufferedReader = new BufferedReader(inStreamReader);
         Scanner scan = new Scanner(System.in);


             System.out.println("Please enter your username");
             input = scan.nextLine();
             printStream.println(input);

             Runnable receiver = () -> {
                 try {
                     String output;
                     output= bufferedReader.readLine();
                     System.out.println("Server>>" + output);

                     if (!output.equals("Name in use."))
                     {
                         while (true) {
                             System.out.println("Server>>" + bufferedReader.readLine());
                         }
                     }
                     System.exit(0);

                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             };

             Thread receive = new Thread(receiver);
             receive.start();


             while (receive.isAlive()) {
                 input = scan.nextLine();
                 printStream.println(input);
             }
         }
}

