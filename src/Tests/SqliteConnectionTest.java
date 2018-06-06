package Tests;

import Database.SqliteConnection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqliteConnectionTest {

    private static String processNumber = "1";

    @Test
    public void testConstructor(){

        SqliteConnection connection = new SqliteConnection(processNumber);

        assertNotNull(connection);
    }

    @Test
    public void testInsertStatement(){
        SqliteConnection connection = new SqliteConnection(processNumber);

        String[] values = {"'RANDOMA'","'RANDOMB'","'RANDOMC'"};

        assertTrue(connection.insert("Messages",values));

    }

    @Test
    public void testSelectStatement(){
        SqliteConnection connection = new SqliteConnection(processNumber);

        assertNotNull(connection.select("SELECT * FROM Messages"));
    }

    @Test
    public void testMax(){
        SqliteConnection connection = new SqliteConnection(processNumber);

        assertNotEquals(-1,connection.max("Messages","ID") );
    }
}
