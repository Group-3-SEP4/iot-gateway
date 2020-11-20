package repository.remoteDataSource;


import models.TeracomModel;
import repository.persistenceDataSource.MsSqlServer;
import util.ApplicationProperties;
import util.Convert;

import java.sql.SQLException;
import java.sql.Timestamp;

import static java.lang.System.exit;


public class LoraWanListenerImpl implements Runnable{

	private static final ApplicationProperties properties = ApplicationProperties.getInstance();
	private WebSocketListenerInterface wsl;

	public LoraWanListenerImpl(String url) {
		wsl = new WebSocketListener(url);
		// check if its possible to connect before receiving actual data. //TODO: Add a check and notify if something went wrong
		MsSqlServer db = new MsSqlServer();
		db.connect(properties.getDbUrl(), properties.getDbUser(), properties.getDbPassword());
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {
				e.printStackTrace();
				exit(1);
			}

			String data = wsl.getMessage();

			var model = Convert.convertJsonToObject(data, TeracomModel.class);
			if (model != null) {

				switch (model.cmd) {
					case "cq":
						for(TeracomModel m : model.cache) {
							Timestamp time = new Timestamp(Long.parseLong(m.ts));
							storeResult(m.eUI, m.data, time);
						}
						break;
					case "rx":
						Timestamp time = new Timestamp(Long.parseLong(model.ts));
						storeResult(model.eUI, model.data, time);
						break;
				}
			}


		}
	}

	private void storeResult(String deviceId, String hexString, Timestamp timestamp) {
		char[] hex = hexString.toCharArray();

		int index = 0;
		int hum = Convert.convertHexByteToInt(hex[index++], hex[index++], hex[index++], hex[index++]);
		int temp = Convert.convertHexByteToInt(hex[index++], hex[index++], hex[index++], hex[index++]);
		int co2 = Convert.convertHexByteToInt(hex[index++], hex[index++], hex[index++], hex[index++]);
		int serv = Convert.convertHexByteToInt(hex[index++], hex[index++], hex[index++], hex[index]);

		System.out.println(String.format("Reading - %s\t- Device {%s} reading: {humidity: %d}, {temperature: %d}, {co2: %d}, {servo: %d}", timestamp, deviceId, hum, temp, co2, serv));

		MsSqlServer db = new MsSqlServer();
		db.connect(properties.getDbUrl(), properties.getDbUser(), properties.getDbPassword());
		try {
			db.insert(deviceId, hum, temp, co2, serv, timestamp);
		} catch (SQLException throwable) {
			throwable.printStackTrace();
		}
	}









}
