

import poc.LoraWanListenerImpl;
import poc.WebSocketListener;
import util.ApplicationProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Main {


    private static ApplicationProperties properties = ApplicationProperties.getInstance();
    private static ExecutorService executorService;

    public static void main(String[] args) {

        String url = properties.getLoraUrl() + properties.getLoraToken();

        LoraWanListenerImpl lw = new LoraWanListenerImpl(url);
        executorService = new ScheduledThreadPoolExecutor(4);
        executorService.submit(lw);


        while(true) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//
//        LoraWanListenerImpl el = new LoraWanListenerImpl( "room1", 1,properties.getLoraUrl() + properties.getLoraToken());
//        el.run();






    }



}
