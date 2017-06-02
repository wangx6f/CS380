import java.io.IOException;

/**
 * Created by wxy03 on 5/31/2017.
 */
public class FileTransfer {

    public static void  main(String[] args)
    {
        if(args[0].equals("makekeys"))
        {
            KeyMaker.makekeys();
        }

        if(args[0].equals("client"))
        {
            Client client = new Client(args[1],args[2],Integer.parseInt(args[3]));
            try {
                client.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(args[0].equals("server"))
        {
            Server server = new Server(args[1],Integer.parseInt(args[2]));
            server.runServer();
        }
    }
}
