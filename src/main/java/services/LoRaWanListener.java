package services;

public interface LoRaWanListener {
    void dataReceivedEvent(String json);
}
