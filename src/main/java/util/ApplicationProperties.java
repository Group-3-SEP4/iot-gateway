package util;

import java.io.IOException;
import java.util.Properties;

public class ApplicationProperties {

    private final Properties properties;
    public ApplicationProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/conf/application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDbUser() {
        return properties.getProperty("db.user");
    }
    public String getDbPassword() {
        return properties.getProperty("db.password");
    }
    public String getDbDriver() {
        return properties.getProperty("db.driver");
    }
    public String getDbUrl() {
        return properties.getProperty("db.url");
    }
    public String getDbTableNameMeasurement() {
        return properties.getProperty("db.tableNameMeasurement");
    }
    public String getDbTableNameConfig() {
        return properties.getProperty("db.tableNameConfig");
    }
    public String getDbTableNameRoom() {
        return properties.getProperty("db.tableNameRoom");
    }
    public int getDbCheckFreqMS() {return Integer.parseInt(properties.getProperty("db.checkFreqMs"));}

    public String getLoraToken() {
        return properties.getProperty("lora.token");
    }
    public String getLoraUrl() { return properties.getProperty("lora.url");
    }



}





