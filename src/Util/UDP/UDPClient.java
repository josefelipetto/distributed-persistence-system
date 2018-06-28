package Util.UDP;

import java.io.IOException;
import java.net.*;

public class UDPClient {

    private DatagramSocket clientSocket;

    private InetAddress IPAddress;

    private int groupPort = 42280;

    private InetAddress groupAddress;

    public UDPClient()
    {

        try
        {
            this.clientSocket = new DatagramSocket();
            this.IPAddress = InetAddress.getByName("localhost");
            this.groupAddress = InetAddress.getByName("224.0.0.1");
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

    public void broadcast(String message)
    {
        try
        {
            MulticastSocket socket = new MulticastSocket(this.groupPort);

            socket.joinGroup(this.groupAddress);

            socket.send(
                    new DatagramPacket(
                            message.getBytes(),
                            message.length(),
                            this.groupAddress,
                            this.groupPort
                    )
            );

            socket.leaveGroup(this.groupAddress);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setTimeout(int timeout)
    {
        try
        {
            this.clientSocket.setSoTimeout(timeout);
        }
        catch (SocketException e)
        {
            System.out.println(" Communication Timeout ");
        }
    }


}
