import java.io.*;
import java.net.Socket;

/**
 * Created by wxy03 on 4/29/2017.
 */
public class Ipv6Client {

    public static void main(String[] args) throws IOException
    {
        Socket socket = new Socket("codebank.xyz",38004);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        for(int i=1;i<13;i++)
        {
            int length = (int)Math.pow((double)2,(double)i);
            System.out.println("data length: "+length);
            os.write(makePacket(length));

            System.out.print("Response: 0x");
            for(int j=0;j<4;j++) {
                System.out.print(Integer.toHexString(is.read()).toUpperCase());
            }
            System.out.println();
        }



    }

    private static byte[] makePacket(int dataLength)
    {
        int totalLength = 40+dataLength;
        byte[] packet = new byte[totalLength];

        packet[0] = (byte)(6<<4);    //version and traffic class
        packet[1] = packet[2] = packet[3] = 0;  //traffic class and flow label

        byte[] total = intToBytes(dataLength);
        packet[4] = total[1];
        packet[5] = total[0]; //total length

        packet[6] = 0x11; //UDP header
        packet[7]=20; //hop limit





        byte[] sourceAddr = extendIpv6(0x18CDB341);
        byte[] destAddr = extendIpv6(0x3425589A);
        for(int i=0;i<16;i++)
        {
            packet[8+i] = sourceAddr[i];
            packet[24+i] = destAddr[i];
        }


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

    private static byte[] extendIpv6(int Ipv4)
    {
        byte[] Ipv6 = new byte[16];
        for(int i = 0;i<10;i++)
        {
            Ipv6[i]=0;
        }
        for(int i = 10;i<12;i++)
        {
            Ipv6[i]=(byte)0xFF;
        }

        byte[] ipv4 = intToBytes(Ipv4);

        Ipv6[12] = ipv4[3];
        Ipv6[13] = ipv4[2];
        Ipv6[14] = ipv4[1];
        Ipv6[15] = ipv4[0];
        return Ipv6;
    }

}
