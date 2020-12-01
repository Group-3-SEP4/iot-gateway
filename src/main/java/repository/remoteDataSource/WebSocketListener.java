package repository.remoteDataSource;

import com.google.gson.Gson;
import models.TeracomModel;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: change naming of the websocketlistener interface to something like RemoteDataSource, so in theory it can be changed to RMI or other remote datasource
public class WebSocketListener implements WebSocket.Listener, WebSocketListenerInterface {

	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private final String url;
	private final ArrayDeque<String> queue;
	private WebSocket server;
	Logger logger = Logger.getLogger(this.getClass().getName());

	public WebSocketListener(String url) {
		queue = new ArrayDeque<>();
		this.url = url;
		connect(url);

		//TODO: Should be seperate method in another class, where it asks for the cache on demand.
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("cmd", "cq");
//		jsonObject.put("page", "1");
//		jsonObject.put("perPage", "1");
//		server.sendText(jsonObject.toString(), true);


	}

	private void connect(String url) {
		HttpClient client = HttpClient.newHttpClient();
		CompletableFuture<WebSocket> ws = client.newWebSocketBuilder()
				.buildAsync(URI.create(url), this);
		server = ws.join();
	}

	// Send down-link message to device
	// Must be in Json format according to https://github.com/ihavn/IoT_Semester_project/blob/master/LORA_NETWORK_SERVER.md
	public void sendMessage(String jsonString) {
		server.sendText(jsonString,true);
	}


	//onOpen()
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
		connect(url);
		return CompletableFuture.completedFuture("onClose() completed.").thenAccept(System.out::println);
	}

	//onPing()
	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
//		logger.log(Level.INFO, "Ping: Client ---> Server. Message: " + message.asCharBuffer().toString()); // TODO: Use another logger, so Debug log can be chosen instead of info to get more graining.
		webSocket.request(1);
		return CompletableFuture.completedFuture("Ping completed.");
//		return CompletableFuture.completedFuture("Ping completed.").thenAccept(System.out::println);
	}

	//onPong()
	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
//		logger.log(Level.INFO, "Pong: Client ---> Server. Message: " + message.asCharBuffer().toString());
		webSocket.request(1);
		return CompletableFuture.completedFuture("Pong completed.");
//		return CompletableFuture.completedFuture("Pong completed.").thenAccept(System.out::println);
	}

	private String text = "";

	//onText()
	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		String message = data.toString().substring(0, Math.min(data.toString().length(), 200)) + " [...]";
		logger.log(Level.INFO, "onText(Last:" + last + "): " + message);
		// As sequence received can be send on multiple occasions, method collects until boolean last is true, then proceeds with the data.

		text += data.toString();
		if (last) {
			processCompleteTextMessage(text);
			text = "";
		}
		webSocket.request(1);

		return CompletableFuture.completedFuture("onText() completed.");
//		return CompletableFuture.completedFuture("onText() completed.").thenAccept(System.out::println);


	}

	private void processCompleteTextMessage(String receivedText) {
		queue.add(receivedText);
	}

	@Override
	public String getMessage() {
		return queue.poll();
	}

}
