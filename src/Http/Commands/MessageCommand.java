package Http.Commands;

import Database.SqliteConnection;
import Http.Server;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MessageCommand extends BaseCommand {

    private ArrayList<Map<String,String>> parameters;

    private Server serverInstance;


    public MessageCommand(String processNumber, Server server)
    {
        super(processNumber);
        this.serverInstance = server;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange httpExchange) throws IOException {

        if( httpExchange.getRequestMethod().equals("POST"))
        {
            this.serverInstance.setiWantToEnterCriticalRegion(true);

            parameters = this.parsePostRequest((Map<String, Object>)httpExchange.getAttribute("parameters"),httpExchange.getRequestBody());

            boolean insertRespose = this.insert(parameters);

            System.out.println("Insert deu certo ? " + Boolean.toString(insertRespose));
        }
    }


    protected boolean insert(ArrayList<Map<String,String>> data) {



        SqliteConnection dbAdapter = new SqliteConnection(this.processNumber);

        String[] values = new String[data.size()];

        int i = 0;

        for(Map<String,String> param : data)
        {
            Map.Entry<String,String> entry = param.entrySet().iterator().next();

            values[i] = entry.getValue();

            i++;
        }

        return dbAdapter.insert("Messages",values);
    }



}
