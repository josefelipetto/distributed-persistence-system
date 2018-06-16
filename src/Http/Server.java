package Http;

import Http.Commands.MessageCommand;
import Util.Timer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Server implements Runnable {

    private String processNumber;

    private Timer timer;

    private boolean iWantToEnterCriticalRegion = false;

    private boolean imAtCriticalRegion = false;

    private Queue<Map<InetAddress,Integer>> reqList;

    private int[] processesPorts;


    public static MessageCommand messageCommand;

    public Server(String processNumber){

        this.processNumber = processNumber;

        this.timer = new Timer();

        reqList = new LinkedList<>();

        if(this.processNumber.equals("1"))
        {
            processesPorts = new int[]{9877,9878};
        }
        else if(this.processNumber.equals("2"))
        {
            processesPorts = new int[]{9876,9878};
        }
        else
        {
            processesPorts = new int[]{9876,9877};
        }

        Thread udpListener = new Thread(new UDPListener(this.processNumber, this));

        udpListener.start();

    }

    @Override
    public void run() {

        HttpServer httpServer;

        try
        {
            httpServer = HttpServer.create(new InetSocketAddress(this.getProcessPort()),0);

            messageCommand = new MessageCommand(this.getProcessNumber(),this);

            httpServer.createContext("/message", messageCommand);

            httpServer.setExecutor(null);
            httpServer.start();
        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();
        }


    }

    public static void respond(int httpCode, String response, HttpExchange httpExchange){

        try
        {
            httpExchange.sendResponseHeaders(httpCode,response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void respondUdp(DatagramPacket receivePacket, String sentence)
    {
        try
        {
            DatagramSocket clientSocket = new DatagramSocket();

            byte[] sendData = new byte[1024];

            sendData = sentence.getBytes();

            clientSocket.send(
                    new DatagramPacket(
                            sendData,
                            sendData.length,
                            receivePacket.getAddress(),
                            receivePacket.getPort()
                    )
            );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public boolean doIWantToEnterCriticalRegion() {
        return iWantToEnterCriticalRegion;
    }

    public void setiWantToEnterCriticalRegion(boolean iWantToEnterCriticalRegion) {
        this.iWantToEnterCriticalRegion = iWantToEnterCriticalRegion;
    }

    public boolean amIAtCriticalRegion() {
        return imAtCriticalRegion;
    }

    public void setImAtCriticalRegion(boolean imAtCriticalRegion) {
        this.imAtCriticalRegion = imAtCriticalRegion;
    }

    public Queue<Map<InetAddress,Integer>> getReqList() {
        return reqList;
    }

    public int[] getProcessesPorts() {
        return processesPorts;
    }

    public Timer getTimer() {
        return timer;
    }

    private int getProcessPort(){

        switch (this.processNumber)
        {
            case "1":
                return 8000;
            case "2":
                return 8001;
            case "3":
                return 8002;
            default:
                System.out.println("Erro ao definir a porta");
                System.exit(0);
                return -1;

        }

    }

    private String getProcessNumber(){
        return this.processNumber;
    }


    static class UDPListener implements Runnable {

        private byte[] receivedData;

        private String processNumber;

        private DatagramSocket serverSocket;

        private String receivedMessage = null;

        private Server serverInstance;

        public UDPListener(String processNumber, Server server){

            this.processNumber = processNumber;
            this.receivedData = new byte[1024];
            this.serverInstance = server;

        }

        @Override
        public void run() {
            try
            {
                this.serverSocket = new DatagramSocket(this.getUDPPort());

                while (true)
                {

                    DatagramPacket receivePacket = new DatagramPacket(this.receivedData,this.receivedData.length);

                    this.serverSocket.receive(receivePacket);

                    receivedMessage = new String(receivePacket.getData());

                    String[] args = receivedMessage.split(":");

                    switch (args[0])
                    {
                        case "REQUEST":

                            if(! this.serverInstance.doIWantToEnterCriticalRegion() && ! this.serverInstance.amIAtCriticalRegion())
                            {
                                this.serverInstance.respondUdp(receivePacket,"OK:" + this.processNumber);
                            }

                            else if( this.serverInstance.amIAtCriticalRegion())
                            {
                                this.serverInstance.getReqList().add(Map.of(receivePacket.getAddress(),receivePacket.getPort()));
                            }

                            else if( this.serverInstance.doIWantToEnterCriticalRegion())
                            {
                                if(this.serverInstance.getTimer().getTimestamp() > Long.parseLong(args[1]))
                                {
                                    this.serverInstance.respondUdp(receivePacket,"OK:" + this.processNumber);
                                }
                                else
                                {
                                    this.serverInstance.getReqList().add(Map.of(receivePacket.getAddress(),receivePacket.getPort()));
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


}
