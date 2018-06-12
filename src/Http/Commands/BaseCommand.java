package Http.Commands;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class BaseCommand implements HttpHandler {

    protected String processNumber;

    public BaseCommand(String processNumber)
    {
        this.processNumber = processNumber;
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

    public static Map<String,String> queryToMap(String query){

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
}
