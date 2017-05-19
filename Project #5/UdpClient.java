import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Created by wxy03 on 4/29/2017.
 */
public class UdpClient {

    public static void main(String[] args) throws IOException
    {

        Socket socket = new Socket("codebank.xyz",38005);
        InputStream is = socket.getInputStream();

        sendData(socket,makeIpPacket(intToBytes(0xDEADBEEF)));

        byte[] receiveMsg = new byte[4];
        is.read(receiveMsg);
        byte[] portNumStream = new byte[2];
        is.read(portNumStream);
        System.out.print("Handshake response: ");
        printBytes(receiveMsg);

        int portNum = readPortNum(portNumStream);
        System.out.println("Port number received: "+portNum);

        int length = 2;
        long startTime;
        long endTime;
        long thisRTT;
        double avgRTT=0;
        for(int i=0;i<12;i++) {

            System.out.println("Sending packet with "+length+" bytes of data");
            byte[] udpPacket = getUdpOverIp(portNum, length);
            receiveMsg = new byte[4];
            startTime = System.currentTimeMillis();
            sendData(socket,udpPacket);
            is.read(receiveMsg);
            endTime = System.currentTimeMillis();
            thisRTT = endTime-startTime;
            avgRTT+=thisRTT;
            System.out.print("Response: ");
            printBytes(receiveMsg);
            System.out.println("RTT: "+ thisRTT+"ms\n");
            length*=2;
        }
        avgRTT/=12;
        System.out.println("Average RTT: "+avgRTT+"ms");






    }


    static private int readPortNum(byte[] stream)
    {
        int port = stream[1] & 0xFF;
        port+= (stream[0] & 0xFF)<<8;
        return port;
    }

    static private void printBytes(byte[] stream)
    {
        System.out.print("0x");
        for(int i=0;i<stream.length;i++)
        {
            System.out.print(Integer.toHexString(stream[i] & 0xFF).toUpperCase());
        }
        System.out.println();

    }

    static private void sendData(Socket socket,byte[] data) throws IOException
    {

        OutputStream os = socket.getOutputStream();
        os.write(data);
    }

    private static byte[] makeIpPacket(byte[] data)
    {
        int totalLength = 20+data.length;
        byte[] packet = new byte[totalLength];

        packet[0] = (byte)((4<<4) +5);    //version and header length
        packet[1] = 0;  //TOS

        byte[] total = intToBytes(totalLength);
        packet[2] = total[2];
        packet[3] = total[3]; //total length
        packet[4]=packet[5]=0;  //Identification
        packet[6] = 1<<6;   //fragment
        packet[7] = 0;  //offset
        packet[8]=50;   //time to live
        packet[9]=17;    //protocol UDP
        packet[10]=packet[11]=0; //checksum
        byte[] sourceAddr = intToBytes(0x18CDB341);

        packet[12]=sourceAddr[0];
        packet[13]=sourceAddr[1];
        packet[14]=sourceAddr[2];
        packet[15]=sourceAddr[3];

        byte[] destAddr = intToBytes(0x3425589A);

        packet[16]=destAddr[0];
        packet[17]=destAddr[1];
        packet[18]=destAddr[2];
        packet[19]=destAddr[3];


        byte[] checkSum = checkSum(packet,20);
        packet[10] = checkSum[2];
        packet[11] = checkSum[3];
        for(int i =20; i<totalLength;i++)
        {
            packet[i] = data[i-20];
        }
        return packet;

    }

    private static byte[] intToBytes(int integer)
    {
        byte[] bytes = new byte[4];
        for(int i=0;i<4;i++)
        {
            bytes[3-i] = (byte)(integer>>(8*i));
        }
        return bytes;
    }

    private static byte[] checkSum(byte[] packet,int length)
    {
        int[] header = new int[length/2];
        int sum = 0;
        for(int i=0;i<header.length;i++)
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

    private static byte[] makeUdpPacket(int destPort,int length)
    {
        int totalLength = 8 +length;
        byte[] packet = new byte[totalLength];

        byte[] source = intToBytes(10000);
        packet[0] = source[2];
        packet[1] = source[3];
        byte[] dest = intToBytes(destPort);
        packet[2] = dest[2];
        packet[3] = dest[3];
        byte[] lengthStream = intToBytes(totalLength);
        packet[4] = lengthStream[2];
        packet[5] = lengthStream[3];

        packet[6] = 0;
        packet[7] = 0;

        byte[] randomData = new byte[length];
        new Random().nextBytes(randomData);
        for(int i=8;i<totalLength;i++)
        {
            packet[i] = randomData[i-8];
        }

        return packet;
    }


    private static byte[] getUdpOverIp(int destPort,int length)
    {
        byte[] udp = makeUdpPacket(destPort,length);
        byte[] ip = makeIpPacket(udp);
        byte[] checkSum = udpCheckSum(udp,ip);
        byte[] actualUdp = new byte[udp.length];

        for(int i=0;i<udp.length;i++)
        {
            if(i==6)
                actualUdp[i] = checkSum[2];
            else if(i==7)
                actualUdp[i] = checkSum[3];
            else
                actualUdp[i] = udp[i];
        }

        return makeIpPacket(actualUdp);
    }

    private static byte[] udpCheckSum(byte[] udp,byte[] ip)
    {
        byte[] pesudo = new byte[12+udp.length];
        //source ipv4
        for(int i=0;i<4;i++)
        {
            pesudo[i] = ip[i+12];
        }
        //dest ipv4
        for(int i=4;i<8;i++)
        {
            pesudo[i] = ip[i+12];
        }

        pesudo[8] = 0;
        pesudo[9] = 17;
        pesudo[10] = udp[4];
        pesudo[11] = udp[5];
        for(int i=12;i<pesudo.length;i++)
        {
            pesudo[i] = udp[i-12];
        }

        return checkSum(pesudo,pesudo.length);
    }
}

