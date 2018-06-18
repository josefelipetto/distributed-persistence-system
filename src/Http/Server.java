package Http;

import Http.Commands.MessageCommand;
import Util.Timer;
import Util.UDP.ProcessUDPListener;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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

        this.processesPorts = this.getProcesses();

        this.timer = new Timer();

        reqList = new LinkedList<>();

        Thread udpListener = new Thread(new ProcessUDPListener(this.processNumber, this));

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

    public Queue<Map<String,Integer>> getReqList() {

        return reqList;
    }

    public void resetReqList() {
        this.reqList = new LinkedList<>();
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

    private int[] getProcesses() {

        if(this.processNumber.equals("1"))
        {
            return new int[]{9877,9878};
        }
        else if(this.processNumber.equals("2"))
        {
            return new int[]{9876,9878};
        }
        else
        {
            return new int[]{9876,9877};
        }

    }


}
