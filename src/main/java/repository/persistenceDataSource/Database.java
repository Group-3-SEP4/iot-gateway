package repository.persistenceDataSource;

import services.DatabaseListener;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface Database extends Runnable{

    void insert(String deviceId, int hum, int temp, int co2, int servo, Timestamp time);
    void regAsListener(DatabaseListener l);

}
