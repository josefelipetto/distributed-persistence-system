package Util.UDP;

import Http.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map;


public class ProcessUDPListener implements Runnable {

    private byte[] receivedData;

    private String processNumber;

    private Server serverInstance;

    public ProcessUDPListener(String processNumber, Server server){

        this.processNumber = processNumber;
        this.receivedData = new byte[1024];
        this.serverInstance = server;

    }

    @Override
    public void run() {
        try
        {
            DatagramSocket serverSocket = new DatagramSocket(this.getUDPPort());

            while (true)
            {

                DatagramPacket receivePacket = new DatagramPacket(this.receivedData,this.receivedData.length);

                serverSocket.receive(receivePacket);

                String receivedMessage = new String(receivePacket.getData(),0,receivePacket.getLength());


                System.out.println("UDP Listener of process " + this.processNumber + " received a message : " + receivedMessage );

                String[] args = receivedMessage.split(":");

                switch (args[0])
                {
                    case "REQUEST":

                        if(! this.serverInstance.doIWantToEnterCriticalRegion() && ! this.serverInstance.amIAtCriticalRegion())
                        {
                            this.serverInstance.respondUdp(
                                    receivePacket,
                                    "OK:" + this.processNumber
                            );
                        }

                        else if( this.serverInstance.amIAtCriticalRegion() )
                        {
                            this.serverInstance.getReqList().add(Map.of(
                                    "ProcessPort",
                                    receivePacket.getPort()
                            ));
                        }

                        else if( this.serverInstance.doIWantToEnterCriticalRegion() )
                        {
                            System.out.println("TS" + args[1]);

                            if(this.serverInstance.getTimer().getTimestamp() > Long.parseLong(args[1]))
                            {
                                this.serverInstance.respondUdp(
                                        receivePacket,
                                        "OK:" + this.processNumber
                                );
                            }
                            else
                            {
                                this.serverInstance.getReqList().add(Map.of(
                                        "ProcessPort",
                                        receivePacket.getPort()
                                ));
                            }
                        }

                        break;
                    case "UPDATE":

                        ArrayList<Map<String,String>> data = this.parseUpdateData(args[1]);
                        break;
                    default:
                        System.out.println("DEFAULT : " + args[0]);
                        break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private int getUDPPort()
    {
        switch (this.processNumber)
        {
            case "1":
                return 9876;
            case "2":
                return 9877;
            case "3":
                return 9878;
        }

        return -1;
    }

    private ArrayList<Map<String,String>> parseUpdateData(String received)
    {
        String list = received.substring(1,received.length() -1);
        String[] pairs = list.split(",");

        ArrayList<Map<String,String>> data = new ArrayList<>();

        for(String pair : pairs)
        {
            String value = ((pair.substring(1,pair.length()-1)).split("="))[1];

            data.add(Map.of("message",value));
        }

        return data;
    }


}
