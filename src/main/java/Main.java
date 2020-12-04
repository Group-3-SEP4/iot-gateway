

import repository.persistenceDataSource.Database;
import repository.persistenceDataSource.MsSqlServerConnection;
import repository.remoteDataSource.LoRaWan;
import repository.remoteDataSource.LoRaWanImpl;
import services.GatewayService;
import util.ApplicationProperties;

public class Main {

    public static void main(String[] args) {
        LoRaWan lrw = new LoRaWanImpl();
        Database db = new MsSqlServerConnection();
        new GatewayService(db, lrw);
    }

}
