package Http;

import Http.Commands.MessageCommand;
import Util.Timer;
import Util.UDP.ProcessUDPListener;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable {

    private String processNumber;

    private Timer timer;

    private boolean iWantToEnterCriticalRegion = false;

    private boolean imAtCriticalRegion = false;

    private Queue<Map<String,Integer>> reqList;

    private int[] processesPorts;

    public static MessageCommand messageCommand;

    public Server(String processNumber){

        this.processNumber = processNumber;

        this.processesPorts = this.getUdpProcessesPorts();

        this.timer = new Timer();

        reqList = new LinkedList<>();

        Thread udpListener = new Thread(
                new ProcessUDPListener(this.processNumber, this)
        );

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

            System.out.println("Process " + this.processNumber + " initiated");

        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();
        }

    }

    public void respondUdp(DatagramPacket receivePacket, String sentence)
    {
        try
        {
            DatagramSocket clientSocket = new DatagramSocket();

            byte[] sendData = sentence.getBytes();

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

        return this.iWantToEnterCriticalRegion;
    }

    public void setiWantToEnterCriticalRegion(boolean iWantToEnterCriticalRegion) {

        this.iWantToEnterCriticalRegion = iWantToEnterCriticalRegion;
    }

    public boolean amIAtCriticalRegion() {

        return this.imAtCriticalRegion;
    }

    public void setImAtCriticalRegion(boolean imAtCriticalRegion) {

        this.imAtCriticalRegion = imAtCriticalRegion;
    }

    public Queue<Map<String,Integer>> getReqList() {

        return this.reqList;
    }

    public void resetReqList() {
        this.reqList = new LinkedList<>();
    }

    public int[] getProcessesPorts() {

        return this.processesPorts;
    }

    public Timer getTimer() {

        return this.timer;
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

    private int[] getUdpProcessesPorts()
    {
        return this.processNumber.equals("1") ? new int[]{9877,9878} : this.processNumber.equals("2") ? new int[]{9876, 9878} : new int[]{9876,9877};
    }


}
