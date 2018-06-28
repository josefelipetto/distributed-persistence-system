package Tests;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InsertServiceTest implements Runnable{

    private int processNumber;

    public InsertServiceTest(int processNumber)
    {
        this.processNumber = processNumber;
    }

    @Override
    public void run() {

        try
        {
            System.out.println("Insert test of process " + Integer.toString(this.processNumber) + " started . ");

            HttpClient httpClient = HttpClients.createDefault();

            for(int i = 0 ; i < 10 ; i++)
            {
                HttpPost httpPost = new HttpPost("http://localhost:" + this.getHttpPort() + "/message");

                List<NameValuePair> params = new ArrayList<NameValuePair>(2);

                params.add(new BasicNameValuePair("message","Mi"+this.random()));

                httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                if(entity != null)
                {
                    System.out.println("Test P " + Integer.toString(this.processNumber) + " received something");

                    InputStream inputStream = entity.getContent();

                    try
                    {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"utf-8");

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        String line = null;

                        System.out.print("Resposta do servidor de persistencia: ");

                        while ( (line = bufferedReader.readLine()) != null )
                        {
                            System.out.println(line);
                        }

                    }
                    finally {
                        inputStream.close();
                    }
                }

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    private String getHttpPort(){

        return this.processNumber == 1 ? "8000" : this.processNumber == 2 ? "8001" : "8002";
    }

    private String random(){

        byte[] array = new byte[126];

        new Random().nextBytes(array);

        return new String(array,Charset.forName("UTF-8"));
    }
}
