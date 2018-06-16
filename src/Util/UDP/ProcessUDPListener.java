package Util.UDP;

import Http.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

                String receivedMessage = new String(receivePacket.getData());

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

                        else if( this.serverInstance.amIAtCriticalRegion())
                        {
                            this.serverInstance.getReqList().add(Map.of(
                                    receivePacket.getAddress(),
                                    receivePacket.getPort()
                            ));
                        }

                        else if( this.serverInstance.doIWantToEnterCriticalRegion())
                        {
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
                                        receivePacket.getAddress(),
                                        receivePacket.getPort()
                                ));
                            }
                        }

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


}
