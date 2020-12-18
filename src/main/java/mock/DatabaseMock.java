package mock;

import repository.persistenceDataSource.Database;

import java.beans.PropertyChangeListener;
import java.sql.Timestamp;

public class DatabaseMock implements Database {
    @Override
    public void insert(String deviceEUI, int hum, int temp, int co2, int servo, Timestamp time) {
        System.out.printf("DB INSERT: eui: %s\thum: %d\ttemp: %d\tco2: %d\tservo:%d\ttimestamp: %s\n",
                deviceEUI,
                hum,
                temp,
                co2,
                servo,
                time);
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        
    }
}
