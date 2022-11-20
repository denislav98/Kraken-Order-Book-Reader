package websocket.message;

/**
 * A contract to fulfill by a class that can process WebSocket messages.
 */
public interface IMessageHandler {
    void handleMessage(String message);
}
