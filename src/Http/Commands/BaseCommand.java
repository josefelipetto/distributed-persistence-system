package Http.Commands;

import Http.Server;
import Util.UDP.UDPClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class BaseCommand implements HttpHandler {

    protected Server serverInstance;

    public BaseCommand(Server serverInstance)
    {
        this.serverInstance = serverInstance;
    }

    protected ArrayList<Map<String,String>> parsePostRequest(Map<String,Object> parameters, InputStream requestBody) throws IOException
    {

        ArrayList<Map<String,String>> parsed = new ArrayList<>();

        InputStreamReader inputStreamReader = new InputStreamReader(requestBody,"utf-8");

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;

        boolean needsToClose = false;

        String paramName = null;

        while ( (line = bufferedReader.readLine()) != null)
        {

            if( line.contains("WebKitFormBoundary") || line.isEmpty())
            {
                continue;
            }

//            if(line.contains("message"))
//            {
//
//                if(line.contains("{") && line.contains("}"))
//                {
//                    line = line.substring(1,line.length()-1);
//                    line = line.replace("\"","");
//                    line = line.replace(":","=");
//                }
//
//                String[] pair = line.split("=");
//
//                parsed.add(Map.of(pair[0],pair[1]));
//
//                continue;
//
//
//            }

            if(needsToClose)
            {
                if( paramName != null )
                {
                    Map<String,String> pair = new HashMap<>();
                    pair.put(paramName,line);
                    parsed.add(pair);
                    paramName = null;
                }
            }

            Pattern wordToFind = Pattern.compile("[a-zA-Z]*=\"[a-zA-Z]*\"");

            Matcher matcher = wordToFind.matcher(line);


            if (matcher.find())
            {
                String[] paramsName = (line.substring(matcher.start()-1,matcher.end())).split("=");
                paramName = paramsName[1].substring(1,paramsName[1].length() - 1);
                needsToClose = true;
            }

        }

        return parsed;

    }

    protected Map<String,String> queryToMap(String query){

        Map<String,String> result = new HashMap<>();

        for(String param : query.split("&"))
        {
            String[] entry = param.split("=");

            if(entry.length > 1)
            {
                result.put(entry[0],entry[1]);
            }
            else
            {
                result.put(entry[0], "");
            }
        }

        return result;
    }

    protected void showRequest(ArrayList<Map<String,String>> parameters){

        System.out.println(" ================== P" + this.serverInstance.getProcessNumber() + " =================");

        for(Map<String,String> param : parameters)
        {
            Map.Entry<String,String> entry = param.entrySet().iterator().next();
            System.out.println(entry.getKey() + ":"+ entry.getValue());
        }

        System.out.println(" ================================================================");
    }


    protected void respond(int httpCode, String response, HttpExchange httpExchange){

        try
        {
            httpExchange.sendResponseHeaders(
                    httpCode,
                    response.length()
            );

            OutputStream outputStream = httpExchange.getResponseBody();

            outputStream.write(
                    response.getBytes()
            );

            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    protected void notifyNodes(String message, Queue<Map<String,Integer>> reqList){
        UDPClient udpClient = new UDPClient();

        for (Map<String,Integer> process : reqList)
        {
            udpClient.send(message ,process.get("ProcessPort"));
        }

    }

    protected boolean update(ArrayList<Map<String,String>> data){

        String message = "UPDATE:" + data.toString();

        UDPClient udpClient = new UDPClient();

        udpClient.setTimeout(2000);

        for( int port : this.serverInstance.getProcessesPorts())
        {

            udpClient.send(message,port);

            String response = udpClient.receive();

            if(response == null)
            {
                return false;
            }

            String[] args = response.split(":");

            if(! args[0].equals("RESULT"))
            {
                System.out.println("Sei la o que deu mano");
                return false;
            }
        }


        return true;
    }

    protected String[] insertFy(ArrayList<Map<String,String>> data )
    {
        int i = 0;

        String[] values = new String[data.size()];

        for(Map<String,String> param : data)
        {
            Map.Entry<String,String> entry = param.entrySet().iterator().next();

            values[i] = entry.getValue();

            i++;
        }

        return values;
    }
}
