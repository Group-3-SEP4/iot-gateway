package repository.persistenceDataSource;

import util.PropertyChangeSubject;

import java.sql.Timestamp;

public interface Database extends PropertyChangeSubject {

    void insert(String deviceEUI, int hum, int temp, int co2, int servo, Timestamp time);

}
