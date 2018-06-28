package Http;

import java.util.concurrent.TimeUnit;

public class Handler {

    public static void main(String[] args){

        Thread p1HttpHandler = new Thread( new Server(1) );
        Thread p2HttpHandler = new Thread( new Server(2) );
        Thread p3HttpHandler = new Thread( new Server(3) );

        p1HttpHandler.start();
        p2HttpHandler.start();
        p3HttpHandler.start();

    }

    private static void delay(long seconds){

        try
        {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
