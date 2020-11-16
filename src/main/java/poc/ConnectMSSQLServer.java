package poc;

import util.ApplicationProperties;

import java.sql.*;

class ConnectMSSQLServer implements IDatabase
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

        String query = "INSERT INTO " + properties.getDbTableName() + "(" +
                "humidity, " +
                "temperature, " +
                "co2, " +
                "time" +
                ") VALUES (" +
                hum + ", " +
                temp + ", " +
                co2 + ", " +
                "GETDATE()" +
                ")";
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        System.out.println("Inserted into database");
        connection.close();
    }

    public static void main(String[] args)
    {

        System.out.println(properties.getDbUser());
        System.out.println(properties.getDbPassword());
        ConnectMSSQLServer connServer = new ConnectMSSQLServer();
        connServer.connect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
    }
}

