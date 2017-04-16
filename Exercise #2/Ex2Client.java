

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Created by wxy03 on 4/15/2017.
 */
public class Ex2Client {

    public static void main(String[] args) throws IOException
    {
        Socket socket = new Socket("codebank.xyz",38102);

        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();


        byte[] inputByte = new byte[100];

        for(int i =0;i<100;i++)
        {
            int byte_one = is.read();
            int byte_two = is.read();
            byte_one = byte_one<<4;
            inputByte[i] = (byte)(byte_one | byte_two);

        }

        System.out.println("Connected to server.\n"+"Received bytes:");

        for(int i =0;i<100;i++)
        {
            System.out.print(String.format("%x",inputByte[i]).toUpperCase());
            if(i%10==9)
                System.out.println();
        }

        CRC32 crc32 = new CRC32();
        crc32.update(inputByte);
        int value = (int)crc32.getValue();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        os.write(buffer.array());

        System.out.println("Generated CRC32: "+String.format("%x",value).toUpperCase());

        int respond = is.read();
        if(respond==1)
            System.out.println("Response good.");
        else
            System.out.println("Response error.");

        System.out.println("Disconnected from server.");
        return;

    }
}
