
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public final class EchoClient {

    public static void main(String[] args) throws Exception {

            Socket socket = new Socket("localhost", 22222);
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            //set up the reader to read input from the server socket as String
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");
            //set up the stream to send output to the server socket as String

            Scanner scan= new Scanner(System.in);

            while(true) {
                System.out.print("Client> ");
                out.println(scan.nextLine());

                String inputString=br.readLine();
                if(inputString.equals("exit"))
                    break;
                else
                    {
                    System.out.print("Server> ");
                    System.out.println(inputString);
                }

            }

            socket.close();
    }
}















