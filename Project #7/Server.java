import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.zip.CRC32;

import static javax.crypto.Cipher.SECRET_KEY;
import static javax.crypto.Cipher.UNWRAP_MODE;

/**
 * Created by wxy03 on 5/31/2017.
 */
public class Server {

    private PrivateKey privateKey;
    private ServerSocket serverSocket;

    public Server(String privateKeyFile,int portNumber)
    {
        try {
            privateKey = (PrivateKey) new ObjectInputStream(new FileInputStream(privateKeyFile)).readObject();
            serverSocket= new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void runServer()
    {
        while(true) {
            try {
                new Thread(new ServerThread(serverSocket.accept())).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerThread implements Runnable{


        private Socket socket;

        private boolean isTransferring;

        private int nextSeqNumExpected;

        private int totalAmountOfChunk;

        private Key sessionKey;

        private String newFileName;

        private FileOutputStream fileOutputStream;

        private ObjectInputStream objectInputStream;

        private ObjectOutputStream objectOutputStream;


        public ServerThread(Socket socket)
        {
            this.socket=socket;
               isTransferring = false;
               resetFileTransfer();
        }

        private void resetFileTransfer()
        {
            isTransferring = false;
            nextSeqNumExpected = -1;
            sessionKey = null;
            if(fileOutputStream!=null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileOutputStream = null;
            }
            totalAmountOfChunk = -1;
        }

        @Override
        public void run() {

            try {
                while (true) {
                    Message incomingMessage = getMessage();
                    if (incomingMessage instanceof StartMessage)
                    {
                        startTransfer((StartMessage) incomingMessage);
                    }
                    else if(incomingMessage instanceof StopMessage)
                    {
                        stopTransfer();
                    }
                    else if(incomingMessage instanceof DisconnectMessage)
                    {
                        break;
                    }
                    else
                    {
                        transferChunk((Chunk) incomingMessage);
                    }
                }
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        private void transferChunk(Chunk chunk)
        {
            if(chunk.getSeq()==nextSeqNumExpected) {

                byte[] data = decrypt(chunk.getData());
                CRC32 crc32 = new CRC32();
                crc32.update(data);
                if((int)crc32.getValue()==chunk.getCrc())
                {
                    nextSeqNumExpected++;
                    try {
                        fileOutputStream.write(data);
                        System.out.println("Chunk received ["+ nextSeqNumExpected+"/"+totalAmountOfChunk+"].");
                        sendMessage(new AckMessage(nextSeqNumExpected));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(nextSeqNumExpected==totalAmountOfChunk) {
                resetFileTransfer();
                System.out.println("Transfer complete.");
                System.out.println("Output path: "+newFileName);
            }
        }

        private byte[] decrypt(byte[] data)
        {
            byte[] result = null;
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE,sessionKey);
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

        private void startTransfer(StartMessage message) throws IOException {
            if(!isTransferring)
            {
                isTransferring = true;
                newFileName = "new_"+message.getFile();
                fileOutputStream = new FileOutputStream(newFileName);
                int chunkSize = message.getChunkSize();
                long fileSize = message.getSize();
                totalAmountOfChunk = (int) (fileSize/chunkSize);
                if(fileSize % chunkSize >0)
                    totalAmountOfChunk++;

                getSessionKey(message.getEncryptedKey());
                nextSeqNumExpected = 0;
                sendMessage(new AckMessage(0));
            }
            else
                sendMessage(new AckMessage(-1));
        }



        private void stopTransfer() throws IOException {
            sendMessage(new AckMessage(-1));
            resetFileTransfer();

        }


        private void getSessionKey(byte[] encryptedKey)
        {
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(UNWRAP_MODE,privateKey);
                sessionKey = cipher.unwrap(encryptedKey,"AES",SECRET_KEY);
                System.out.println();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }


        }


        private Message getMessage() throws IOException, ClassNotFoundException {

            if(objectInputStream==null)
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            return (Message) objectInputStream.readObject();

        }

        private void sendMessage(Message message) throws IOException {
            if(objectOutputStream==null)
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
        }
    }

}
