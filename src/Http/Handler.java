package Http;

import java.util.concurrent.TimeUnit;

public class Handler {

    public static void main(String[] args){

        Thread p1HttpHandler = new Thread(new Server("1"));
        p1HttpHandler.start();

        delay(1);
        Thread p2HttpHandler = new Thread(new Server("2"));
        p2HttpHandler.start();

        delay(1);
        Thread p3HttpHandler = new Thread(new Server("3"));
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
