import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wxy03 on 6/5/2017.
 */
public class WebServer {
    public static void main(String[] args)
    {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true)
            {
                try(Socket socket = serverSocket.accept())
                {

                    respond(socket);


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void respond(Socket socket) {
        int respondCode;
        long length;
        try {

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String firstLine = reader.readLine();
            String[] temp = firstLine.split(" ");

            File file = new File("./www" + temp[1]);

            if (file.exists())
                respondCode = 200;
            else {
                respondCode = 404;
                file = new File("./www/error.html");
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            length = file.length();

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println("HTTP/1.1 " + respondCode + " OK");
            printWriter.println("Content-type: text/html");
            printWriter.println("Content-length: " + length);
            printWriter.println("");


            String line = bufferedReader.readLine();
            while (line != null) {
                printWriter.println(line);
                line = bufferedReader.readLine();
            }
            printWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
