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

         do {
             System.out.println("Please enter your username");
             input = scan.nextLine();
             printStream.print(input);
         }while(socket.isClosed());

         Runnable receiver =()->{
             while(true) {
                 try {
                     System.out.println(bufferedReader.readLine());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         };

         new Thread(receiver).start();


         while(true)
         {
             System.out.print("Client>>");
             input=scan.nextLine();
             printStream.print(input);
         }

     }
}
