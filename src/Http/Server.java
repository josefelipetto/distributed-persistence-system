package Http;

import Http.Commands.MessageCommand;
import Util.Config.BasicConfig;
import Util.Timer;
import Util.UDP.GroupListener;
import Util.UDP.ProcessUDPListener;
import Util.UDP.UDPClient;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

    private int processNumber;

    private Timer timer;

    private boolean iWantToEnterCriticalRegion = false;

    private boolean imAtCriticalRegion = false;

    private Queue<Map<String,Integer>> reqList = new LinkedList<>();

    private List<Integer> processesPorts = new ArrayList<>();

    private UDPClient udpClient = new UDPClient();

    private int clientsReady = 1;

    public Server(int processNumber){

        this.processNumber = processNumber;

        this.timer = new Timer();

        Thread udpListener = new Thread( new ProcessUDPListener( this) );

        Thread groupListener = new Thread( new GroupListener(this) );

        udpListener.start();

        groupListener.start();

        this.timer.start();

    }

    @Override
    public void run() {

        while (this.clientsReady < BasicConfig.NUMBER_OF_NODES)
        {
            this.udpClient.broadcast("START:" + Integer.toString( this.getProcessNumber() ) + "," + this.udpPort());
            System.out.println("Process " + this.processNumber + " not ready yet");
        }

        HttpServer httpServer;

        try
        {
            httpServer = HttpServer.create(new InetSocketAddress(this.getHttpPort()),0);

            httpServer.createContext("/message", new MessageCommand(this));

            httpServer.setExecutor(null);
            httpServer.start();

            System.out.println("Process " + this.processNumber + " initiated");

        }
        catch (IOException | NullPointerException e)
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

    public List<Integer> getProcessesPorts() {

        return this.processesPorts;
    }

    public Timer getTimer() {

        return this.timer;
    }

    private int getHttpPort(){

        switch (this.processNumber)
        {
            case 1:
                return 8000;
            case 2:
                return 8001;
            case 3:
                return 8002;
            default:
                System.out.println("Erro ao definir a porta");
                System.exit(0);
                return -1;

        }

    }

    public int getProcessNumber(){

        return this.processNumber;
    }

    private void delay(long seconds){

        try
        {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private int udpPort(){
        return this.processNumber == 1 ? 9876 : this.processNumber == 2 ? 9877 : 9878;
    }

    public void addClientsReady() {
        this.clientsReady++;
    }

    public boolean checkForOtherNodes(){

        UDPClient udp = new UDPClient();

        udp.setTimeout(5000);

        int responseNumber = 0;

        for(int port : this.getProcessesPorts())
        {
            udp.send("PING", port);

            String response = udp.receive();

            if(response == null)
            {
                return false;
            }

            responseNumber++;
        }


        return responseNumber >= BasicConfig.NUMBER_OF_NODES - 1;
    }
}
