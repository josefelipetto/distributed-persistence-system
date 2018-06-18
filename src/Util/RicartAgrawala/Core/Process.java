package Util.RicartAgrawala.Core;

import Http.Server;
import Util.UDP.UDPClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;

public class Process {

    private String processNumber;

    private Server server;


    public Process(String processNumber, Server server){

        this.processNumber = processNumber;
        this.server = server;
    }

    public boolean proceed()
    {
        boolean canProceed = false;

        UDPClient udpClient = new UDPClient();

        int numberOfResponses = 0;

        for (int processPort : this.server.getProcessesPorts())
        {
            udpClient.send(
                    "REQUEST:" + Long.toString(this.server.getTimer().getTimestamp()),
                    processPort
            );

            String response = udpClient.receive();

            String[] args = response.split(":");

            if(args[0].equals("OK") && ! args[1].equals(this.processNumber))
            {
                numberOfResponses++;
            }
        }

        if(numberOfResponses >= 2)
        {
            canProceed = true;
        }

        return canProceed;
    }
}
