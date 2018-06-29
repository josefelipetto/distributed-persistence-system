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
        UDPClient udpClient = new UDPClient();

        for (int processPort : this.server.getProcessesPorts())
        {
            udpClient.send(
                    "REQUEST:" + Long.toString(this.server.getTimer().getTimestamp()),
                    processPort
            );

            String response = udpClient.receive();

            if( response == null )
            {
                return false;
            }

            String[] args = response.split(":");

            if(! args[0].equals("OK") && ! args[1].equals(Integer.toString(this.server.getProcessNumber())))
            {
                return false;
            }
        }

        return true;
    }

    public void setTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isTimeOut() {
        return timeOut;
    }
}
