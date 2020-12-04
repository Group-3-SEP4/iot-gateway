package services;


import models.ConfigModel;
import models.TeracomModel;
import org.json.JSONObject;
import repository.persistenceDataSource.Database;
import repository.remoteDataSource.LoRaWan;
import util.Convert;
import util.EventTypes;

import java.beans.PropertyChangeEvent;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;


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

		byte[] data = new byte[6];

		data[0] = (byte) ((config.tempSetpoint >> 8) & 0xFF);
		data[1] = (byte) (config.tempSetpoint & 0xFF);
		data[2] = (byte) ((config.co2Min >> 8) & 0xFF);
		data[3] = (byte) (config.co2Min & 0xFF);
		data[4] = (byte) ((config.co2Max >> 8) & 0xFF);
		data[5] = (byte) (config.co2Max & 0xFF);

		StringBuilder hex = new StringBuilder();

		for (byte b : data){
			hex.append(String.format("%02X", b));
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("cmd", "tx");
		jsonObject.put("EUI", config.eui);
		jsonObject.put("port", 2);
		jsonObject.put("data", hex.toString());

		lrw.sendMessage(jsonObject.toString());
	}
}