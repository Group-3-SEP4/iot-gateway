package poc;

import util.ApplicationProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class ConnectMSSQLServer
{
    private static final ApplicationProperties properties = ApplicationProperties.getInstance();

    public void dbConnect(String db_connect_string,
                          String db_userid,
                          String db_password)
    {
        try {
            Class.forName(properties.getDbDriver());
            Connection conn = DriverManager.getConnection(db_connect_string, db_userid, db_password);
            System.out.println("connected");
            Statement statement = conn.createStatement();
            String queryString = "select * from " + properties.getDbTableName();
            ResultSet rs = statement.executeQuery(queryString);
            while (rs.next()) {
                System.out.println("id: " + rs.getString(1) + " name: " + rs.getString(2));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {

        System.out.println(properties.getDbUser());
        System.out.println(properties.getDbPassword());
        ConnectMSSQLServer connServer = new ConnectMSSQLServer();
        connServer.dbConnect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
    }
}

