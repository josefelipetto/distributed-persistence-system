package Database;

import java.sql.ResultSet;

interface Connectable {

    boolean insert(String table, String[] values);

    ResultSet select(String query);

    int max(String table, String identifier);


}
