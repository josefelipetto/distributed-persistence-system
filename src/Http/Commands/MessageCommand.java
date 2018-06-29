package Http.Commands;

import Database.SqliteConnection;
import Http.Server;
import Util.Config.BasicConfig;
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
                        BasicConfig.SERVER_ERROR,
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
                        BasicConfig.OK,
                        (worked) ? "Inserido com sucesso" : "Erro ao inserir",
                        httpExchange
                );


                this.notifyNodes(
                        "OK:" + Integer.toString(this.serverInstance.getProcessNumber()),
                        this.serverInstance.getReqList()
                );

                System.out.println("Inserido com sucesso");

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

            String response = "No record";

            long start = System.currentTimeMillis();

            while (response.equals("No record") && (System.currentTimeMillis() <= start + 3000L))
            {
                ResultSet resultSet = dbAdapter.select("SELECT * FROM Messages WHERE id = " + value);

                try
                {
                    response = resultSet.getString("MESSAGE");
                }
                catch (SQLException e)
                {
//                    System.out.println("Não achou. Tentando de novo...");
                }
            }

            System.out.println(response);

            this.respond(
                    response.equals("No record") ? BasicConfig.NOT_FOUND : BasicConfig.OK,
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

        // Fake delay injection to simulate real world applications and it's delivery issues

        if(BasicConfig.DEBUG)
        {
            long randomDelay = 1L + (long) (Math.random() * 4L);

            this.delay(randomDelay);
        }

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
