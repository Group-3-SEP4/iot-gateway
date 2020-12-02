package repository.remoteDataSource;

import services.LoRaWanListener;

public interface LoRaWan {
    void sendMessage(String json);
    void regAsListener(LoRaWanListener l);
}
