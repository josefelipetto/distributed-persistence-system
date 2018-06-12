package Http;

import Database.SqliteConnection;
import Http.Commands.MessageCommand;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {

    private String processNumber;

    public Server(String processNumber){

        this.processNumber = processNumber;

    }

    @Override
    public void run() {

         HttpServer httpServer = null;

        try
        {
            httpServer = HttpServer.create(new InetSocketAddress(this.getProcessPort()),0);

            httpServer.createContext("/message", new MessageCommand(this.getProcessNumber()));

            httpServer.setExecutor(null);
            httpServer.start();
        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();
        }

    }

    public static void respond(int httpCode, String response, HttpExchange httpExchange){

        try
        {
            httpExchange.sendResponseHeaders(httpCode,response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private int getProcessPort(){

        switch (this.processNumber)
        {
            case "1":
                return 8000;
            case "2":
                return 8001;
            case "3":
                return 8002;
            default:
                System.out.println("Erro ao definir a porta");
                System.exit(0);
                return -1;

        }

    }

    private String getProcessNumber(){
        return this.processNumber;
    }

}
