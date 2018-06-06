package Database;

public class SqliteConnection extends DatabaseConnection {

    public SqliteConnection(String processNumber){

        super(processNumber,"org.sqlite.JDBC");

    }

}
