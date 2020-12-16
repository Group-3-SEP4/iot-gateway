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
import java.time.ZoneId;
import java.time.ZonedDateTime;


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
						storeResult(m.eUI, m.data, CETtoUTC(m.ts));
					}
					break;
				case "rx":
					storeResult(model.eUI, model.data, CETtoUTC(model.ts));
					break;
			}
		}
	}

	private Timestamp CETtoUTC(String ts) {
		var instant = Instant.ofEpochMilli(Long.parseLong(ts));
		var z1dt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
		return Timestamp.valueOf(z1dt.toLocalDateTime());
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