package Tests;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GetServiceTest extends Testable implements Runnable {

    public GetServiceTest(int processNumber){
        super(processNumber);
    }

    @Override
    public void run() {

        HttpClient httpClient = HttpClients.createDefault();

        for(int i = 0 ; i < 10; i ++)
        {
            int random = i + (int)(Math.random() * 3*i);
            HttpGet httpGet = new HttpGet("http://localhost:" + this.getHttpPort() + "/message?id=" + Integer.toString(random));

            try
            {
                HttpResponse httpResponse = httpClient.execute(httpGet);

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent())
                );

                String line = null;

                System.out.print("Resposta GET: ");

                while ((line = bufferedReader.readLine()) != null)
                {
                    System.out.println(line);
                }


            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}
