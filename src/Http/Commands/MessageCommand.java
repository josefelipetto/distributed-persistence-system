package Http.Commands;

import Database.SqliteConnection;
import Http.Server;
import Util.RicartAgrawala.Core.Process;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MessageCommand extends BaseCommand {

    private ArrayList<Map<String,String>> parameters;

    public MessageCommand(String processNumber, Server server)
    {
        super(processNumber, server);
    }

    private  Process mutualExclusionHandler = new Process(
            this.processNumber,
            this.serverInstance
    );

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange httpExchange) throws IOException {

        System.out.println(httpExchange.getRequestMethod());

        if( httpExchange.getRequestMethod().equals("POST"))
        {
            System.out.println("MessageCommand of " + this.processNumber + ": I've received a message to store in database");

            parameters = this.parsePostRequest((Map<String, Object>)httpExchange.getAttribute("parameters"),httpExchange.getRequestBody());

            this.serverInstance.setiWantToEnterCriticalRegion(true);

            while (! this.mutualExclusionHandler.proceed() )
            {
                System.out.println("Blocked for new inserts");
            }

            if(this.mutualExclusionHandler.isTimeOut())
            {
                this.respond(
                        200,
                        "Timeout em um dos processos",
                        httpExchange
                );
            }
            else
            {
                this.serverInstance.setImAtCriticalRegion(true);

                System.out.println("¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨");
                System.out.println("Process " + this.processNumber + " status ");
                System.out.println("Intent : " + this.serverInstance.doIWantToEnterCriticalRegion() );
                System.out.println("ImIn   : " + this.serverInstance.amIAtCriticalRegion());
                System.out.println("¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨¨");

                System.out.println("MAASSSSS : " + Boolean.toString(this.serverInstance.amIAtCriticalRegion()));

                boolean worked = false;

                if( this.replicate() )
                {
                    worked = true;

                    this.insert(parameters);
                }

                this.respond(
                        200,
                        (worked) ? "Inserido com sucesso" : "Erro ao inserir",
                        httpExchange
                );


                this.notifyNodes(
                        "OK:" + this.processNumber,
                        this.serverInstance.getReqList()
                );

                System.out.println("Vai sair");

                this.serverInstance.resetReqList();

                this.serverInstance.setImAtCriticalRegion(false);
            }

            this.serverInstance.setiWantToEnterCriticalRegion(false);
        }
        else if(httpExchange.getRequestMethod().equals("GET"))
        {
            Map<String,String> query = this.queryToMap(httpExchange.getRequestURI().getQuery());

            String key = query.entrySet().iterator().next().getKey();
            String value = query.entrySet().iterator().next().getValue();

            SqliteConnection dbAdapter = new SqliteConnection(this.processNumber);

            ResultSet resultSet = dbAdapter.select("SELECT * FROM Messages WHERE id = " + value);

            String response = "No record";

            try
            {
                response = resultSet.getString("MESSAGE");
            }
            catch (SQLException e)
            {
//                e.printStackTrace();
            }
            this.respond(
                    200,
                    response,
                    httpExchange
            );
        }
    }

    private boolean insert(ArrayList<Map<String,String>> data) {

        this.serverInstance.setImAtCriticalRegion(true);

        SqliteConnection dbAdapter = new SqliteConnection(this.processNumber);

        String[] values = this.insertFy(data);

        boolean success =  dbAdapter.insert("Messages",values);

        this.serverInstance.setImAtCriticalRegion(false);

        return success;
    }

    private boolean replicate(){

        // Update
        System.out.println("Process " + this.processNumber + " fired an update request ");

        long randomDelay = 1L + (long) (Math.random() * 4L);

        randomDelay = 5L; // TESTE

        this.delay(randomDelay);

        return this.update(parameters);

    }

    private void delay(long seconds){

        try
        {
            TimeUnit.SECONDS.sleep(seconds);
            System.out.println("Delay acabou");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }



}
