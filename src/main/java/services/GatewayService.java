package services;


import models.ConfigModel;
import models.TeracomModel;
import org.json.JSONObject;
import repository.persistenceDataSource.Database;
import repository.remoteDataSource.LoRaWan;
import util.Convert;
import util.EventTypes;

import java.beans.PropertyChangeEvent;
import java.sql.Timestamp;
import java.time.Instant;


public class GatewayService {

	private final Database db;
	private final LoRaWan lrw;


	public GatewayService(Database database, LoRaWan loRaWan) {
		db = database;
		lrw = loRaWan;
		registerAsListener();
	}


	private void registerAsListener(){
		db.addPropertyChangeListener(EventTypes.NEW_CONFIGURATION_AVAILABLE.toString(), this::configurationReceivedEvent);
		lrw.addPropertyChangeListener(EventTypes.NEW_LORA_DATA_RECEIVED.toString(), this::dataReceivedEvent);
	}


	private void dataReceivedEvent(PropertyChangeEvent event){
		String json = (String) event.getNewValue();
		var model = Convert.convertJsonToObject(json, TeracomModel.class);
		if (model != null) {

			switch (model.cmd) {
				case "cq":
					for(TeracomModel m : model.cache) {
						Timestamp time = Timestamp.from(Instant.parse(m.time));
						storeResult(m.eUI, m.data, time);
					}
					break;
				case "rx":
					Timestamp time = Timestamp.from(Instant.parse(model.time));
					storeResult(model.eUI, model.data, time);
					break;
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

		System.out.printf("Reading - %s\t- Device {%s} reading: {humidity: %d}, {temperature: %d}, {co2: %d}, {servo: %d}%n", timestamp, deviceId, hum, temp, co2, serv);

		db.insert(deviceId, hum, temp, co2, serv, timestamp);
	}


	private void configurationReceivedEvent(PropertyChangeEvent event) {
		ConfigModel config = (ConfigModel) event.getNewValue();

		String hex = String.format("%04X", config.tempSetpoint) +
				String.format("%04X", config.co2Min) +
				String.format("%04X", config.co2Max);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("cmd", "tx");
		jsonObject.put("EUI", config.eui);
		jsonObject.put("port", 2);
		jsonObject.put("data", hex);

		lrw.sendMessage(jsonObject.toString());
	}
}