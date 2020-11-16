package poc;

import java.sql.SQLException;

public interface IDatabase {



    void connect(String db_connect_string, String db_userid, String db_password) throws SQLException;
    boolean isConnected() throws SQLException;
    void insert(int humidity, int temperature, int co2) throws SQLException;
}
