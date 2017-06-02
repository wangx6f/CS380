
import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.CRC32;

/**
 * Created by wxy03 on 6/1/2017.
 */
public class Client {


    private Socket socket;

    private Key publicKey;

    private Key sessionKey;

    private ObjectInputStream objectInputStream;

    private ObjectOutputStream objectOutputStream;

    public Client(String publicKeyFile,String host,int port)
    {
        try {
            publicKey = (PublicKey) new ObjectInputStream(new FileInputStream(publicKeyFile)).readObject();
            socket = new Socket(host,port);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void run() throws IOException {
        while(true) {

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter path: ");
            String fileName = scanner.nextLine();
            System.out.print("Enter chunk size: ");
            int chunkSize = scanner.nextInt();
            generateSessionKey();
            List<Chunk> data = getData(chunkSize,fileName);

            try {
                sendMessage(new StartMessage(fileName, getEncryptedSessionKey(), chunkSize));
                System.out.println("Sending: "+fileName+". File size "+new File(fileName).length()+".");
                System.out.println("Sending "+data.size()+" chunks.");

            } catch (IOException e) {
                e.printStackTrace();
            }



            while(true)
            {
                try {
                    AckMessage ackMessage = (AckMessage) getMessage();
                     int receivedSeqNum = ackMessage.getSeq();

                    if(ackMessage.getSeq()==-1) {
                        System.out.println("Server error!");
                       break;
                    }
                    else if(receivedSeqNum==data.size())
                        break;
                    else
                    {
                        System.out.println("Chunk completed ["+(receivedSeqNum+1)+"/"+data.size()+"]");
                        sendMessage(data.get(receivedSeqNum));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Start a new file transfer? y/n");
            scanner.nextLine();
            String choice = scanner.nextLine().toLowerCase();
            if(choice!="y")
            {
                sendMessage(new DisconnectMessage());
                break;
            }

        }
    }



    private List<Chunk> getData(int chunkSize, String fileName) throws IOException {

            FileInputStream fileInputStream = new FileInputStream(fileName);
            List<Chunk> chunkList = new ArrayList<>();

            long totalLength = new File(fileName).length();

            int chunkAmount = (int) (totalLength / chunkSize);

            int lastChunkSize = (int) (totalLength % chunkSize);
            if (lastChunkSize==0)
                lastChunkSize = chunkSize;
            else
                chunkAmount++;


            for (int i = 0; i < chunkAmount; i++) {
                byte[] thisSegment;
                if(i==chunkAmount-1)
                    thisSegment = new byte[lastChunkSize];
                else
                    thisSegment = new byte[chunkSize];
                fileInputStream.read(thisSegment);
                if(i==chunkAmount-1)
                {

                }
                CRC32 crc32 = new CRC32();
                crc32.update(thisSegment);
                thisSegment = encrypt(thisSegment);
                chunkList.add(new Chunk(i, thisSegment, (int) crc32.getValue()));
            }



            return chunkList;


    }

    private byte[] encrypt(byte[] data)
    {
        byte[] result = null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,sessionKey);
            result = cipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return result;

    }



    private Message getMessage() throws IOException, ClassNotFoundException {

        if(objectInputStream==null)
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        return (Message) objectInputStream.readObject();

    }

    private void sendMessage(Message message) throws IOException {
        if(objectOutputStream ==null)
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
    }

    private void generateSessionKey()
    {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sessionKey = keyGenerator.generateKey();
    }

    private byte[] getEncryptedSessionKey()
    {
        byte[] encryptedKey = null;
        try {

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.WRAP_MODE,publicKey);
            encryptedKey =  cipher.wrap(sessionKey);
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encryptedKey;
    }
}
