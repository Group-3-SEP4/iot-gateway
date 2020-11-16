package poc;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketListener implements WebSocket.Listener {
	public static String STATUS = "WebSocket is closed";

	private final ArrayDeque<String> queue;
	private WebSocket server = null;


	public WebSocketListener(String url) {
		queue = new ArrayDeque<>();
		HttpClient client = HttpClient.newHttpClient();
		CompletableFuture<WebSocket> ws = client.newWebSocketBuilder()
				.buildAsync(URI.create(url), this);

		server = ws.join();


	}



	// Send down-link message to device
	// Must be in Json format according to https://github.com/ihavn/IoT_Semester_project/blob/master/LORA_NETWORK_SERVER.md
	public void sendDownLink(String jsonTelegram) {
		server.sendText(jsonTelegram,true);
	}



	//onOpen()
	public void onOpen(WebSocket webSocket) {
		// This WebSocket will invoke onText, onBinary, onPing, onPong or onClose methods on the associated listener (i.e. receive methods) up to n more times
		webSocket.request(1);
		System.out.println("WebSocket Listener has been opened for requests.");
	}

	//onError()
	public void onError​(WebSocket webSocket, Throwable error) {
		System.out.println("A " + error.getCause() + " exception was thrown.");
		System.out.println("Message: " + error.getLocalizedMessage());
		webSocket.abort();
	}

	//onClose()
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		System.out.println("WebSocket closed!");
		System.out.println("Status:" + statusCode + " Reason: " + reason);
		return CompletableFuture.completedFuture("onClose() completed.").thenAccept(System.out::println);
	}

	//onPing()
	public CompletionStage<?> onPing​(WebSocket webSocket, ByteBuffer message) {
		webSocket.request(1);
		System.out.println("Ping: Client ---> Server");
		System.out.println(message.asCharBuffer().toString());
		return CompletableFuture.completedFuture("Ping completed.").thenAccept(System.out::println);
	}

	//onPong()
	public CompletionStage<?> onPong​(WebSocket webSocket, ByteBuffer message) {
		webSocket.request(1);
		System.out.println("Pong: Client ---> Server");
		System.out.println(message.asCharBuffer().toString());
		return CompletableFuture.completedFuture("Pong completed.").thenAccept(System.out::println);
	}

	//onText()
	public CompletionStage<?> onText​(WebSocket webSocket, CharSequence data, boolean last) {
		String indented = (new JSONObject(data.toString())).toString(4);
		System.out.println(indented);
		queue.add(indented);
		webSocket.request(1);
		return CompletableFuture.completedFuture("onText() completed.").thenAccept(System.out::println);
	}

	public String getMessage() {
		return queue.poll();
	}
}
