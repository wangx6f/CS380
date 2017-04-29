import java.io.*;
import java.net.Socket;

/**
 * Created by wxy03 on 4/29/2017.
 */
public class Ipv4Client {

    public static void main(String[] args) throws IOException
    {
        Socket socket = new Socket("codebank.xyz",38003);
        InputStream is = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        OutputStream os = socket.getOutputStream();
        for(int i=1;i<13;i++)
        {
            int length = (int)Math.pow((double)2,(double)i);
            System.out.println("data length: "+length);
            os.write(makePacket(length));
            System.out.println(br.readLine());
        }



    }

    private static byte[] makePacket(int dataLength)
    {
        int totalLength = 20+dataLength;
        byte[] packet = new byte[totalLength];

        packet[0] = (byte)((4<<4) +5);    //version and header length
        packet[1] = 0;  //TOS

        byte[] total = intToBytes(totalLength);
        packet[2] = total[1];
        packet[3] = total[0]; //total length
        packet[4]=packet[5]=0;  //Identification
        packet[6] = 1<<6;   //fragment
        packet[7] = 0;  //offset
        packet[8]=50;   //time to live
        packet[9]=6;    //protocol TCP
        packet[10]=packet[11]=0; //checksum
        byte[] sourceAddr = intToBytes(0x18CDB341);

        packet[12]=sourceAddr[3];
        packet[13]=sourceAddr[2];
        packet[14]=sourceAddr[1];
        packet[15]=sourceAddr[0];

        byte[] destAddr = intToBytes(0x3425589A);

        packet[16]=destAddr[3];
        packet[17]=destAddr[2];
        packet[18]=destAddr[1];
        packet[19]=destAddr[0];

        for(int i=20;i<totalLength;i++)
        {
            packet[i]=0;
        }

        byte[] checkSum = checkSum(packet);
        packet[10] = checkSum[1];
        packet[11] = checkSum[0];
        return packet;

    }

    private static byte[] intToBytes(int integer)
    {
        byte[] bytes = new byte[4];
        for(int i=0;i<4;i++)
        {
            bytes[i] = (byte)(integer>>(8*i));
        }
        return bytes;
    }

    private static byte[] checkSum(byte[] packet)
    {
        int[] header = new int[10];
        int sum = 0;
        for(int i=0;i<10;i++)
        {
            int first = ((int)packet[2*i]<<8)&0xFF00;
            int second =packet[2*i+1] & 0xFF;
            header[i] = first+second;
            sum+=header[i];
        }

        sum = (sum & 0xFFFF)+(sum>>16);
        sum = ~sum;

        return intToBytes(sum);

    }
}
