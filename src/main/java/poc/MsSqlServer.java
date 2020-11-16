package poc;

import util.ApplicationProperties;

import java.sql.*;

class MsSqlServer implements SqlServer
{
    private static final ApplicationProperties properties = ApplicationProperties.getInstance();
    private Connection connection;


    @Override
    public void connect(String db_connect_string, String db_userid, String db_password)
    {
        try {
            Class.forName(properties.getDbDriver());
            connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);
            System.out.println("connected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() throws SQLException{
        return !connection.isClosed();
    }

    @Override
    public void insert(int hum, int temp, int co2) throws SQLException {

        String query = String.format("INSERT INTO %s (humidity, temperature, co2, time) VALUES (%d, %d, %d, GETDATE())", properties.getDbTableName(), hum, temp, co2);
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        System.out.println("Inserted into database");
        connection.close();
    }

    // test sql
    public static void main(String[] args)
    {
        System.out.println(properties.getDbUser());
        System.out.println(properties.getDbPassword());
        MsSqlServer connServer = new MsSqlServer();
        connServer.connect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
    }
}

