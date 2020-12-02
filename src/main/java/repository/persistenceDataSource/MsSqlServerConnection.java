package repository.persistenceDataSource;

import models.ConfigModel;
import services.DatabaseListener;
import util.ApplicationProperties;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsSqlServerConnection implements Database {
    private DatabaseListener listener = null;
    private final ApplicationProperties properties;
    private final Logger logger;
    private Connection connection;


    public MsSqlServerConnection(ApplicationProperties applicationProperties){
        properties = applicationProperties;
        logger = Logger.getLogger(this.getClass().getName());
    }


    @Override
    public void regAsListener(DatabaseListener l) {
        if (listener == null){
            listener = l;
        }
    }


    public void connect()
    {
        try {
            Class.forName(properties.getDbDriver());
            connection = DriverManager.getConnection(properties.getDbUrl(), properties.getDbUser(), properties.getDbPassword());
//            System.out.println("connected to database at: " + db_connect_string);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isConnected() throws SQLException{
        return !connection.isClosed();
    }


    @Override
    public void insert(String deviceId, int hum, int temp, int co2, int servo, Timestamp time) {
        connect();
        try {
            if(isConnected()){
                String query = String.format("INSERT INTO %s (timestamp, humidityPercentage, carbonDioxide, temperature, servoPositionPercentage, deviceId) VALUES ('%s', %d, %d, %d, %d, '%s')", properties.getDbTableNameMeasurement(), time, hum, co2, temp, servo, deviceId);
                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
                System.out.println("Inserted into database at " + LocalDateTime.now());
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(properties.getDbCheckFreqMS());
                connect();

                if(isConnected()){
                    String query = String.format("SELECT r.deviceEUI, s.settingsId, s.temperatureSetpoint, s.ppmMin, s.ppmMax " +
                            "FROM %s s " +
                            "JOIN %s r ON s.settingsId = s.settingsId " +
                            "WHERE s.sentToDevice is NULL;", properties.getDbTableNameConfig(), properties.getDbTableNameRoom());

                    Statement statement = connection.createStatement();

                    ResultSet rs = statement.executeQuery(query);
                    connection.commit();

                    ArrayList<ConfigModel> configUpdates = new ArrayList<>();

                    while(rs.next()) {
                        ConfigModel config = new ConfigModel();

                        config.id = rs.getInt("settingsId");
                        config.eui = rs.getString("deviceEUI");
                        config.temperatureSetpoint = rs.getInt("temperatureSetpoint");
                        config.co2Min = rs.getInt("ppmMin");
                        config.co2Max = rs.getInt("ppmMax");

                        configUpdates.add(config);
                    }


                    if(configUpdates.size() > 0){
                        logger.log(Level.INFO, "Configurations updated amount: " + configUpdates.size());
                        for (ConfigModel c : configUpdates) {
                            listener.configurationReceivedEvent(c);
                            updateDbConfigSent(c);
                        }
                    }

                    statement.close();
                    rs.close();
                    connection.close();
                }
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void updateDbConfigSent(ConfigModel sentConfig) throws SQLException {
        String query = String.format("UPDATE %s " +
                "SET sentToDevice =  GETDATE()" +
                "WHERE settingsId = %s;", properties.getDbTableNameConfig(), sentConfig.id);

        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }


//    // test sql
//    public static void main(String[] args)
//    {
//        System.out.println(properties.getDbUser());
//        System.out.println(properties.getDbPassword());
//        MsSqlServer connServer = new MsSqlServer();
//        connServer.connect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
//    }
}

