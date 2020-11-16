package poc;


import org.json.JSONObject;
import util.ApplicationProperties;

import java.sql.SQLException;

import static java.lang.System.exit;


public class LoraWanListenerImpl implements Runnable{

	private static final ApplicationProperties properties = ApplicationProperties.getInstance();
	private WebSocketListener wsl;

	public LoraWanListenerImpl(String url) {
		wsl = new WebSocketListener(url);
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
			JSONObject obj = new JSONObject(data);


			if(data != null) {
				char[] hex = obj.getString("data").toCharArray();
				int hum = Integer.parseInt("" + hex[0] + hex[1] + hex[2] + hex[3], 16);
				int temp = Integer.parseInt("" + hex[4] + hex[5]+ hex[6]+ hex[7], 16);
				int co2 = Integer.parseInt("" + hex[8] + hex[9] + hex[10] + hex[11], 16);

				System.out.println(String.format("Recieved:\t Humidity: %d, Temperature: %d, Co2: %d", hum, temp, co2));

				MsSqlServer db = new MsSqlServer();
				db.connect(properties.getDbUrl(), properties.getDbUser(),properties.getDbPassword());
				try {
					db.insert(hum, temp, co2);
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}

			}
		}
	}
}
