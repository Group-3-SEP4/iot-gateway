package repository.persistenceDataSource;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface Database {

    void connect(String db_connect_string, String db_userid, String db_password) throws SQLException;
    boolean isConnected() throws SQLException;
    void insert(String deviceId, int hum, int temp, int co2, int servo, Timestamp time) throws SQLException;
    void read();
}
