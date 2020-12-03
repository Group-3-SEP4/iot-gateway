

import repository.persistenceDataSource.Database;
import repository.persistenceDataSource.MsSqlServerConnection;
import repository.remoteDataSource.LoRaWan;
import repository.remoteDataSource.LoRaWanImpl;
import services.GatewayService;
import util.ApplicationProperties;

public class Main {

    public static void main(String[] args) {
        ApplicationProperties p = new ApplicationProperties();
        LoRaWan lrw = new LoRaWanImpl(p);
        Database db = new MsSqlServerConnection(p);
        new GatewayService(db, lrw);
    }

}
