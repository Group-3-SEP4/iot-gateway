package repository.persistenceDataSource;

import models.ConfigModel;
import util.ApplicationProperties;
import util.EventTypes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsSqlServerConnection implements Database {
    private final ApplicationProperties properties;
    private final PropertyChangeSupport support;
    private final Logger logger;
    private Connection connection;
    private boolean configsRequested;


    public MsSqlServerConnection(ApplicationProperties applicationProperties){
        properties = applicationProperties;
        support = new PropertyChangeSupport(this);
        logger = Logger.getLogger(this.getClass().getName());
        configsRequested = false;
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


    private boolean isConnected() throws SQLException{
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
                statement.close();
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
                if(configsRequested){
                    Thread.sleep(properties.getDbCheckFreqMS());
                }
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
                        config.tempSetpoint = rs.getInt("temperatureSetpoint");
                        config.co2Min = rs.getInt("ppmMin");
                        config.co2Max = rs.getInt("ppmMax");

                        configUpdates.add(config);
                    }


                    if(configUpdates.size() > 0){
                        for (ConfigModel configuration : configUpdates) {
                            support.firePropertyChange(EventTypes.NEW_CONFIGURATION_AVAILABLE.toString(), "", configuration);
                            updateDbConfigTimeStamp(configuration);
                        }
                        logger.log(Level.INFO, "Configurations updated amount: " + configUpdates.size());
                    }

                    configsRequested = true;
                    statement.close();
                    rs.close();
                    connection.close();
                }
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void updateDbConfigTimeStamp(ConfigModel sentConfig) throws SQLException {
        String query = String.format("UPDATE %s " +
                "SET sentToDevice =  GETDATE()" +
                "WHERE settingsId = %s;", properties.getDbTableNameConfig(), sentConfig.id);

        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        if (name == null){
            support.addPropertyChangeListener(listener);
        } else {
            support.addPropertyChangeListener(name, listener);
        }
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

