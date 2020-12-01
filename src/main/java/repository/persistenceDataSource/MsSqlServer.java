package repository.persistenceDataSource;

import util.ApplicationProperties;

import java.sql.*;
import java.time.LocalDateTime;

public class MsSqlServer implements Database
{
    private static final ApplicationProperties properties = ApplicationProperties.getInstance();
    private Connection connection;


    @Override
    public void connect(String db_connect_string, String db_userid, String db_password)
    {
        try {
            Class.forName(properties.getDbDriver());
            connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);
//            System.out.println("connected to database at: " + db_connect_string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() throws SQLException{
        return !connection.isClosed();
    }

    @Override
    public void insert(String deviceId, int hum, int temp, int co2, int servo, Timestamp time) throws SQLException {

        String query = String.format("INSERT INTO %s (timestamp, humidityPercentage, carbonDioxide, temperature, servoPositionPercentage, deviceId) VALUES ('%s', %d, %d, %d, %d, '%s')", properties.getDbTableName(), time, hum, co2, temp, servo, deviceId);
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        System.out.println("Inserted into database at " + LocalDateTime.now());
        connection.close();
    }

    @Override
    public void read() {

    }

    // test sql TODO: remove before final release
    public static void main(String[] args)
    {
        System.out.println(properties.getDbUser());
        System.out.println(properties.getDbPassword());
        MsSqlServer connServer = new MsSqlServer();
        connServer.connect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
    }
}

