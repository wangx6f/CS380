
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class EchoServer {



    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            while (true)
            {
                Socket socket = serverSocket.accept();

                //override run method of Runnable
                Runnable serverThread = () -> {
                    String address = socket.getInetAddress().getHostAddress();
                    System.out.printf("Client connected: %s%n", address);   //connection established
                    try {
                        InputStream is = socket.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                        BufferedReader br = new BufferedReader(isr);
                        //set up the reader to read input from the client socket as String

                        OutputStream os = socket.getOutputStream();
                        PrintStream out = new PrintStream(os, true, "UTF-8");
                        //set up the stream to send output to the client socket as String

                        while (true) {
                            String inputString = br.readLine();   //read the input from client socket
                            //blocked while reach EOF
                            out.println(inputString);
                            System.out.println("received a new message from client: "+address);
                            //send back the input string to client
                            if (inputString.equals("exit"))
                                break;
                        }

                        socket.close();
                        System.out.printf("Client disconnected: %s%n", address);    //the connection is closed
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };  //end of the serverThread

                new Thread(serverThread).start();  //once the serverSocket accepted a connection from a client,
                //a new serverThread is established
            }
        }
    }
}
