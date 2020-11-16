package poc;


import static java.lang.System.exit;


public class LoraWanListenerImpl implements Runnable{


	WebSocketListener wsl;
	public LoraWanListenerImpl(String url) {
		wsl = new WebSocketListener(url);
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				e.printStackTrace();
				exit(1);
			}
			String data = wsl.getMessage();
			if (data != null) {
				System.out.println("message: " + data);
			}






//			if(update != null) {
////				char[] hex = Document.parse(update).getString("data").toCharArray();
////				int hum = Integer.parseInt("" + hex[0] + hex[1], 16);
////				int temp = Integer.parseInt("" + hex[2] + hex[3], 16);
////				int co2 = Integer.parseInt("" + hex[4] + hex[5] + hex[6] + hex[7], 16);
////				int light = Integer.parseInt("" + hex[8] + hex[9] + hex[10] + hex[11], 16);
////				int water = Integer.parseInt("" + hex[12] + hex[13], 16);
//
//			}
		}
	}
}
