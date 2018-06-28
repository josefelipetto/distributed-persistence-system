package Util.RicartAgrawala.Core;

import Http.Server;
import Util.UDP.UDPClient;

public class Process {

    private Server server;

    private boolean timeOut = false;

    public Process(Server server){

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

            System.out.println("RESSSP ->  "+response);

            String[] args = response.split(":");

            if(args[0].equals("OK") && ! args[1].equals(Integer.toString(this.server.getProcessNumber())))
            {
                numberOfResponses++;
            }
        }

        if(numberOfResponses >= 2)
        {
            System.out.println("Pode ir fera");
            canProceed = true;
        }

        return canProceed;
    }

    public void setTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isTimeOut() {
        return timeOut;
    }
}
