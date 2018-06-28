package Util.UDP;

import Http.Server;

abstract public class Listenable {

    protected Server serverInstance;

    public Listenable(Server server)
    {
        this.serverInstance = server;
    }

    protected int getUDPPort()
    {
        switch (this.serverInstance.getProcessNumber())
        {
            case 1:
                return 9876;
            case 2:
                return 9877;
            case 3:
                return 9878;
        }

        return -1;
    }

}
