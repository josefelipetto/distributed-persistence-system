package Util.UDP;

import Http.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class GroupListener extends Listenable implements Runnable{

    private int groupPort = 42280;

    private InetAddress groupAddress;


    public GroupListener(Server server){

        super(server);

        try
        {
            this.groupAddress = InetAddress.getByName("224.0.0.1");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try
        {

            MulticastSocket multicastSocket = new MulticastSocket(this.groupPort);

            multicastSocket.joinGroup(groupAddress);

            while (true)
            {
                byte[] buffer = new byte[1000];

                DatagramPacket receivePacket = new DatagramPacket(buffer,buffer.length);

                multicastSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

//                System.out.println("Process " + this.server.getProcessNumber() + " received a group message : " + message );

                String[] args = message.split(":");

                switch (args[0])
                {
                    case "START":

                        String[] processInfo = args[1].split(",");

                        if( ! Integer.toString(this.serverInstance.getProcessNumber()).equals(processInfo[0]) && ! this.serverInstance.getProcessesPorts().contains(Integer.parseInt(processInfo[1])))
                        {
                            this.serverInstance.getProcessesPorts().add( Integer.parseInt(processInfo[1]) );

                            this.serverInstance.addClientsReady();
                        }

                        break;
                }

            }

        }
        catch (IOException e )
        {
            e.printStackTrace();
        }


    }


}
