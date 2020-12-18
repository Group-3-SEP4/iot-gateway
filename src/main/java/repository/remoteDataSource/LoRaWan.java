package repository.remoteDataSource;

import util.PropertyChangeSubject;

import java.net.http.WebSocket;

public interface LoRaWan extends WebSocket.Listener, PropertyChangeSubject {
    void sendMessage(String json);
}
