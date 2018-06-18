package Http.Commands;

import Database.SqliteConnection;
import Http.Server;
import Util.RicartAgrawala.Core.Process;
import Util.UDP.UDPClient;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

public class MessageCommand extends BaseCommand {

    private ArrayList<Map<String,String>> parameters;

    public MessageCommand(String processNumber, Server server)
    {
        super(processNumber, server);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(HttpExchange httpExchange) throws IOException {

        if( httpExchange.getRequestMethod().equals("POST"))
        {
            System.out.println("MessageCommand: I've received a message to store in database");

            this.serverInstance.setiWantToEnterCriticalRegion(true);

            parameters = this.parsePostRequest((Map<String, Object>)httpExchange.getAttribute("parameters"),httpExchange.getRequestBody());

            Process mutualExclusionHandler = new Process(
                    this.processNumber,
                    this.serverInstance
            );


            while (! mutualExclusionHandler.proceed() )
            {
                System.out.println("Waiting for OK Responses");
            }

            System.out.println("It's okay to proceed");

            boolean insertResponse = this.insert(parameters);

            this.serverInstance.setImAtCriticalRegion(true);

            // Update
            System.out.println("Update below");
            this.update(parameters);

            this.notifyNodes(
                    "OK:" + this.processNumber,
                    this.serverInstance.getReqList()
            );

            this.serverInstance.resetReqList();

            this.serverInstance.setiWantToEnterCriticalRegion(false);
            this.serverInstance.setImAtCriticalRegion(false);

            this.respond(
                    200,
                    (insertResponse) ? "Inserido com sucesso" : "Erro ao inserir",
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



}
