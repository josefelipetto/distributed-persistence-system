package Util.UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    private DatagramSocket clientSocket;

    private InetAddress IPAddress;

    public UDPClient()
    {

        try
        {
            this.clientSocket = new DatagramSocket();
            this.IPAddress = InetAddress.getByName("localhost");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }

    public void send(String message, int processPort)
    {
        try
        {

            byte sendData[] = message.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,this.IPAddress,processPort);

            this.clientSocket.send(sendPacket);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public String receive()
    {
        byte receiveData[] = new byte[1024];

        String response = null;

        try
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);

            this.clientSocket.receive(receivePacket);

            response = new String(receivePacket.getData(),0,receivePacket.getLength());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return response;


    }


}
