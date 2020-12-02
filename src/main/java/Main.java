

import repository.persistenceDataSource.Database;
import repository.persistenceDataSource.MsSqlServerConnection;
import repository.remoteDataSource.LoRaWan;
import repository.remoteDataSource.LoRaWanImpl;
import services.GatewayService;
import util.ApplicationProperties;

public class Main {

    public static void main(String[] args) {
        ApplicationProperties p = new ApplicationProperties();

        Database db = new MsSqlServerConnection(p);
        Thread dbThread = new Thread(db);
        dbThread.start();

        LoRaWan lrw = new LoRaWanImpl(p);
        new GatewayService(db, lrw);

    }

}
