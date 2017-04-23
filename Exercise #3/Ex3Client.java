import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by wxy03 on 4/22/2017.
 */
public class Ex3Client {

    public static void main(String[] args) throws IOException
    {
        Socket socket = new Socket("codebank.xyz",38103);

        System.out.println("Connected to server.");
        InputStream is = socket.getInputStream();

        int size = is.read();
        System.out.println("Reading "+size+" bytes");

        int[] receivedData = new int[size];


        for(int i=0;i<size;i++)
        {
            receivedData[i] = is.read();
        }
        if(size%2==1)
        {
            receivedData= Arrays.copyOf(receivedData,size+1);
            receivedData[size]=0;
        }

        System.out.println("Data received:");

        for(int i=0;i<receivedData.length;i++)
        {
            System.out.print(Integer.toHexString(receivedData[i]).toUpperCase());
            if(i%11==10)
                System.out.println();
        }
        System.out.println();

        socket.getOutputStream().write(checkSum(receivedData));

        int respond = is.read();
        if(respond==1)
            System.out.println("Response good.");
        else
            System.out.println("Response error.");


    }

    public static byte[] checkSum(int[] data)
    {
        int[] processedData = new int[data.length/2];
        for(int i=0;i<data.length;i+=2)
        {
            processedData[i/2]=(data[i]<<8)+data[i+1];
        }

        int sum = 0;

        for(int i=0;i<processedData.length;i++)
        {
            sum+=processedData[i];
            if((sum & 0xFFFF0000)!=0)
            {
                sum &= 0xFFFF;
                sum++;
            }
        }
        sum = -(sum & 0xFFFF)-1;
        System.out.println("Checksum calculated: "+Integer.toHexString(sum).toUpperCase().substring(4));
        byte[] result = new byte[2];
        result[0] = (byte)((sum>>8) & 0xFF);
        result[1] = (byte)(sum &0xFF);
        return result;
    }
}
