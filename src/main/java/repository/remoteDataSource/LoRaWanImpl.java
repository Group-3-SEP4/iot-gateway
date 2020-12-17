package repository.remoteDataSource;

import util.ApplicationProperties;
import util.EventTypes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoRaWanImpl implements LoRaWan {

    private final ScheduledExecutorService executorService;
    private final PropertyChangeSupport support;
    private final Logger logger;
    private final String url;
    private WebSocket server;
    private String data = "";

    public LoRaWanImpl() {
        executorService = Executors.newScheduledThreadPool(1);
        support = new PropertyChangeSupport(this);
        logger = Logger.getLogger(this.getClass().getName());
        url = ApplicationProperties.getInstance().getLoraUrl() + ApplicationProperties.getInstance().getLoraToken();
        connect();
    }

    private void connect() {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<WebSocket> ws = client.newWebSocketBuilder()
                .buildAsync(URI.create(url), this);
        server = ws.join();
    }

    // Send down-link message to device
    // Must be in Json format according to https://github.com/ihavn/IoT_Semester_project/blob/master/LORA_NETWORK_SERVER.md
    @Override
    public void sendMessage(String json) {
        server.sendText(json, true);
    }

    //onOpen()
    @Override
    public void onOpen(WebSocket webSocket) {
        // This WebSocket will invoke onText, onBinary, onPing, onPong or onClose methods on the associated listener (i.e. receive methods) up to n more times
        webSocket.request(1);

        //keep connection alive
        executorService.scheduleAtFixedRate(() -> {
                    String data = "Ping";
                    ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
                    server.sendPing(payload);
                },
                1, 1, TimeUnit.MINUTES);

        logger.log(Level.INFO, "WebSocket Listener has been opened for requests.");
    }

    //onError()
    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.log(Level.INFO, "A " + error.getCause() + " exception was thrown. Aborting socket.");
        webSocket.abort();
    }

    //onClose()
    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        logger.log(Level.INFO, "WebSocket closed!" + ", Status:" + statusCode + ", Reason: " + reason);
        //reconnect
        connect();
        return CompletableFuture.completedFuture("onClose() completed.").thenAccept(System.out::println);
    }

    //onPing()
    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return CompletableFuture.completedFuture("Ping completed.");
    }

    //onPong()
    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        webSocket.request(1);
        return CompletableFuture.completedFuture("Pong completed.");
    }

    //onText()
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString().substring(0, Math.min(data.toString().length(), 200)) + " [...]";
        logger.log(Level.INFO, "onText(Last:" + last + "): " + message);
        // As sequence received can be send on multiple occasions, method collects until boolean last is true, then proceeds with the data.
        this.data += data.toString();
        if (last) {
            support.firePropertyChange(EventTypes.NEW_LORA_DATA_RECEIVED.toString(), "", this.data);
            this.data = "";
        }
        webSocket.request(1);

        return CompletableFuture.completedFuture("onText() completed.");
    }

    @Override
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        if (name == null) {
            support.addPropertyChangeListener(listener);
        } else {
            support.addPropertyChangeListener(name, listener);
        }
    }
}
