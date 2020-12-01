import repository.remoteDataSource.LoraWanListenerImpl;
import util.ApplicationProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Main {


    private static ApplicationProperties properties = ApplicationProperties.getInstance();
    private static ExecutorService executorService;

    public static void main(String[] args) {



        String url = properties.getLoraUrl() + properties.getLoraToken();
        executorService = new ScheduledThreadPoolExecutor(4);
        LoraWanListenerImpl lw = new LoraWanListenerImpl(url);
        executorService.submit(lw);


        while(true) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }



}
