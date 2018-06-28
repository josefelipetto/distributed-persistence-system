package Util.UDP;

import Database.SqliteConnection;
import Http.Server;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ProcessUDPListener extends Listenable implements Runnable {

    private byte[] receivedData;

    private UDPClient udpClient = new UDPClient();


    public ProcessUDPListener(Server server){

        super(server);

        this.receivedData = new byte[1024];
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

//                System.out.println("UDP Listener of process " + this.serverInstance.getProcessNumber() + " received a message : " + receivedMessage );

                String command = receivedMessage;
                String value = null;

                if( receivedMessage.contains(":"))
                {
                    String[] args = receivedMessage.split(":");

                    command = args[0];

                    value = args[1];
                }

                if( ! command.equals(receivedMessage) && value == null)
                {
                    System.out.println("Wrong message");

                    continue;
                }

                switch (command)
                {
                    case "REQUEST":

                        if(! this.serverInstance.doIWantToEnterCriticalRegion() && ! this.serverInstance.amIAtCriticalRegion())
                        {

                            this.udpClient.send(
                                    "OK:" + Integer.toString(this.serverInstance.getProcessNumber()),
                                    receivePacket.getPort()
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

                            if(this.serverInstance.getTimer().getTimestamp() > Long.parseLong(value))
                            {
                                this.udpClient.send(
                                        "OK:" + Integer.toString(this.serverInstance.getProcessNumber()),
                                        receivePacket.getPort()
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

                        ArrayList<Map<String,String>> data = this.parseUpdateData(value);

                        SqliteConnection sqliteConnection = new SqliteConnection(this.serverInstance.getProcessNumber());

                        boolean response = sqliteConnection.insert("Messages",this.insertFy(data));

                        this.udpClient.send(
                                "RESULT:" + Boolean.toString(response),
                                receivePacket.getPort()
                        );

                        break;
                    case "PING":

                        this.udpClient.send(
                                "LIVE:" + Integer.toString(this.serverInstance.getProcessNumber()),
                                receivePacket.getPort()
                        );
                        break;

                    default:
                        System.out.println("DEFAULT : " + command);
                        break;

                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private ArrayList<Map<String,String>> parseUpdateData(String received)
    {
        String list = received.substring(1,received.length() -1);

        ArrayList<Map<String,String>> data = new ArrayList<>();

        String value = ((list.substring(1,list.length()-1)).split("="))[1];

        data.add(Map.of("message",value));

        return data;
    }

    private String[] insertFy(ArrayList<Map<String,String>> data )
    {
        int i = 0;

        String[] values = new String[data.size()];

        for(Map<String,String> param : data)
        {
            Map.Entry<String,String> entry = param.entrySet().iterator().next();

            values[i] = entry.getValue();

            i++;
        }

        return values;
    }


}
