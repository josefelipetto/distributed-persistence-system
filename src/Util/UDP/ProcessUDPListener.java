package Util.UDP;

import Database.SqliteConnection;
import Http.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ProcessUDPListener implements Runnable {

    private byte[] receivedData;

    private String processNumber;

    private Server serverInstance;

    public ProcessUDPListener(String processNumber, Server server){

        this.processNumber = processNumber;
        this.receivedData = new byte[1024];
        this.serverInstance = server;

        System.out.println("//////////////////////////////////");
        System.out.println("Process " + this.processNumber + " status ");
        System.out.println("Intent : " + this.serverInstance.doIWantToEnterCriticalRegion() );
        System.out.println("ImIn   : " + this.serverInstance.amIAtCriticalRegion());
        System.out.println("//////////////////////////////////");

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
                        System.out.println("================================================");
                        System.out.println("Process " + this.processNumber + " status ");
                        System.out.println("Intent : " + this.serverInstance.doIWantToEnterCriticalRegion() );
                        System.out.println("ImIn   : " + this.serverInstance.amIAtCriticalRegion());
                        System.out.println("================================================");

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

                        SqliteConnection sqliteConnection = new SqliteConnection(this.processNumber);

                        boolean response = sqliteConnection.insert("Messages",this.insertFy(data));

                        System.out.println("Update P" + this.processNumber + " : " + Boolean.toString(response));

                        this.serverInstance.respondUdp(receivePacket,"RESULT:" + Boolean.toString(response));

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
