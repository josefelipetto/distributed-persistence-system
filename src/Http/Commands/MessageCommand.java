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

    public MessageCommand(Server server)
    {
        super(server);
    }

    private  Process mutualExclusionHandler = new Process( this.serverInstance );

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange httpExchange) throws IOException {

        if( httpExchange.getRequestMethod().equals("POST"))
        {

            parameters = this.parsePostRequest((Map<String, Object>)httpExchange.getAttribute("parameters"),httpExchange.getRequestBody());

            if ( ! this.serverInstance.checkForOtherNodes() )
            {

                System.out.println("One of the processes is offline ");

                this.respond(
                        500,
                        "Timeout em um dos processos",
                        httpExchange
                );
            }
            else
            {
                this.serverInstance.setiWantToEnterCriticalRegion(true);

                while (! this.mutualExclusionHandler.proceed() )
                {
                    System.out.println("Blocked for new inserts");
                }

                this.serverInstance.setImAtCriticalRegion(true);

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
                        "OK:" + Integer.toString(this.serverInstance.getProcessNumber()),
                        this.serverInstance.getReqList()
                );

                this.serverInstance.resetReqList();

                this.serverInstance.setImAtCriticalRegion(false);

                this.serverInstance.setiWantToEnterCriticalRegion(false);
            }

        }
        else if(httpExchange.getRequestMethod().equals("GET"))
        {
            Map<String,String> query = this.queryToMap(httpExchange.getRequestURI().getQuery());

            String value = query.entrySet().iterator().next().getValue();

            SqliteConnection dbAdapter = new SqliteConnection(this.serverInstance.getProcessNumber());

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

        SqliteConnection dbAdapter = new SqliteConnection(this.serverInstance.getProcessNumber());

        String[] values = this.insertFy(data);

        boolean success =  dbAdapter.insert("Messages",values);

        this.serverInstance.setImAtCriticalRegion(false);

        return success;
    }

    private boolean replicate(){


        long randomDelay = 1L + (long) (Math.random() * 4L);

        randomDelay = 5L; // TESTE

        this.delay(randomDelay);

        return this.update(parameters);

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
