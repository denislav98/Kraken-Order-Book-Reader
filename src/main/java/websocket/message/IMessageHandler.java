package websocket.message;

import java.util.Map;

import model.OrderBook;

/**
 * A contract to fulfill by a class that can process WebSocket messages.
 */
public interface IMessageHandler {

    Map<String, OrderBook> handleMessage(String message);
}
