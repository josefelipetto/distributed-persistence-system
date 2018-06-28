package Database;

public class SqliteConnection extends DatabaseConnection {

    public SqliteConnection(int processNumber){

        super(processNumber,"org.sqlite.JDBC");

    }

}
