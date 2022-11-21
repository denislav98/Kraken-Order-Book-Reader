package websocket.message;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static websocket.message.WebSocketMessagePayloadHelper.createOrderBook;
import static websocket.message.WebSocketMessagePayloadHelper.getOrderBookPair;
import static websocket.message.WebSocketMessagePayloadHelper.updateOrderBook;

import java.util.Map;
import java.util.TreeMap;

import model.OrderBook;

public class WebSocketMessageHandler implements IMessageHandler {

    private final Map<String, OrderBook> orderBookMap;

    public WebSocketMessageHandler() {
        orderBookMap = new TreeMap<>();
    }

    public Map<String, OrderBook> handleMessage(String message) {
        String orderBookPair = getOrderBookPair(message);

        if (isEmpty(orderBookPair)) {
            return emptyMap();
        }

        OrderBook book = orderBookMap.get(orderBookPair);
        if (book == null) {
            orderBookMap.put(orderBookPair, createOrderBook(message));
        } else {
            updateOrderBook(message, book);
            orderBookMap.put(orderBookPair, book);
        }

        return orderBookMap;
    }
}
