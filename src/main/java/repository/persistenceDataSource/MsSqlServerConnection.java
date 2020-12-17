package repository.persistenceDataSource;

import models.ConfigModel;
import util.ApplicationProperties;
import util.EventTypes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsSqlServerConnection implements Database {
    private final ScheduledExecutorService executorService;
    private final ApplicationProperties properties;
    private final PropertyChangeSupport support;
    private final Logger logger;
    private Connection connection;


    public MsSqlServerConnection(){
        executorService = Executors.newScheduledThreadPool(1);
        properties = ApplicationProperties.getInstance();
        support = new PropertyChangeSupport(this);
        logger = Logger.getLogger(this.getClass().getName());
        checkDbConfigurations();
    }


    public void connect()
    {
        try {
            Class.forName(properties.getDbDriver());
            connection = DriverManager.getConnection(properties.getDbUrl(), properties.getDbUser(), properties.getDbPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isConnected() throws SQLException{
        return !connection.isClosed();
    }


    @Override
    public void insert(String deviceEUI, int hum, int temp, int co2, int servo, Timestamp time) {
        connect();
        try {
            if(isConnected()){
                String query = String.format("INSERT INTO %s (timestamp, humidityPercentage, carbonDioxide, temperature, servoPositionPercentage, deviceEUI) VALUES ('%s', %d, %d, %d, %d, '%s')", properties.getDbTableNameMeasurement(), time, hum, co2, temp, servo, deviceEUI);
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

    private void checkDbConfigurations(){
        executorService.scheduleAtFixedRate(() -> {
                    try {
                        connect();

                        if(isConnected()){
                            String query = String.format("SELECT r.deviceEUI, s.settingsId, s.temperatureSetpoint, s.ppmMin, s.ppmMax " +
                                    "FROM %s s " +
                                    "JOIN %s r ON s.settingsId = s.settingsId " +
                                    "WHERE s.sentToDevice is NULL OR s.sentToDevice < s.lastUpdated;", properties.getDbTableNameConfig(), properties.getDbTableNameRoom());

                            Statement statement = connection.createStatement();

                            ResultSet rs = statement.executeQuery(query);

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
                            } else {
                                logger.log(Level.INFO, "Database checked for new configurations: No updates found.");
                            }

                            statement.close();
                            rs.close();
                            connection.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                },
                0, properties.getDbCheckMinutes(), TimeUnit.MINUTES);
    }


    private void updateDbConfigTimeStamp(ConfigModel sentConfig) throws SQLException {
        String query = String.format("UPDATE %s " +
                "SET sentToDevice = GETDATE()" +
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
}

