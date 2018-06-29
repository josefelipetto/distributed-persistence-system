package Tests;

import java.util.concurrent.TimeUnit;

public class ParallelTest {
    public ParallelTest(){

        this.delay(2);

        Thread p1InsertTest = new Thread( new InsertServiceTest(1));
        Thread p2InsertTest = new Thread( new InsertServiceTest(2));
        Thread p3InsertTest = new Thread( new InsertServiceTest(3));

        Thread p1GetTest = new Thread( new GetServiceTest(1));
        Thread p2GetTest = new Thread( new GetServiceTest(2));
        Thread p3GetTest = new Thread( new GetServiceTest(3));

        p1InsertTest.start();
        p2InsertTest.start();
        p3InsertTest.start();

        p1GetTest.start();
        p2GetTest.start();
        p3GetTest.start();

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
}
