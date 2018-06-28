package Database;

import org.sqlite.SQLiteException;

import java.sql.*;

public class DatabaseConnection implements Connectable {

    protected Connection connection = null;

    protected int currentProcess;

    protected Statement statement = null;

    public DatabaseConnection(int currentProcess, String driver) {

        try
        {
            this.currentProcess = currentProcess;

            Class.forName(driver);

            this.connection = DriverManager.getConnection(this.getConnectionString());
            this.connection.setAutoCommit(false);

        }
        catch (SQLException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public boolean insert(String table, String[] values) {

        try
        {
            this.statement = this.connection.createStatement();

            String st = this.generateInsertStatement(table,values);

            if(st == null)
            {
                return false;
            }

            this.statement.executeUpdate(st);

            this.statement.close();

            this.connection.commit();

            return  true;
        }
        catch (SQLException e)
        {
            return false;
        }

    }

    @Override
    public ResultSet select(String query) {

        ResultSet resultSet = null;

        try
        {
            this.statement = this.connection.createStatement();

            resultSet = this.statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return  resultSet;
    }

    @Override
    public int max(String table, String identifier) {

        ResultSet resultSet = this.select("SELECT MAX(" + identifier + ") AS maxId FROM " + table);

        if(resultSet == null)
        {
            return -1;
        }

        try
        {
            return resultSet.getInt("maxId");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    private String getConnectionString(){

        return ( ("jdbc:sqlite:src/DBFiles/DB").concat(Integer.toString(currentProcess)) ).concat(".db");

    }

    private String generateInsertStatement(String table, String[] values){

        String query = "INSERT INTO " + table + " VALUES";

        int higherIdentifier = this.max(table,"ID") + 1;

        if(higherIdentifier == -1)
        {
            return null;
        }

        int size = values.length;

        int i = 0;

        String endOfLine = ",";

        for(String value : values)
        {
            if( i++ == size - 1)
            {
                endOfLine = "";
            }

            query = query.concat("(" + Integer.toString(higherIdentifier) + ",'" + value + "')" + endOfLine);

            higherIdentifier++;
        }

        return  query;
    }



}
