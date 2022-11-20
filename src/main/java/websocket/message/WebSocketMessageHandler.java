package websocket.message;

import static websocket.message.WebSocketMessagePayloadHelper.buildOrderBook;
import static websocket.message.WebSocketMessagePayloadHelper.getOrderBookPair;
import static websocket.message.WebSocketMessagePayloadHelper.updateOrderBook;

import java.util.Map;
import java.util.TreeMap;

import model.OrderBook;
import console.AppConsolePrinter;

public class WebSocketMessageHandler implements IMessageHandler {

    private final Map<String, OrderBook> orderBookMap;

    public WebSocketMessageHandler() {
        orderBookMap = new TreeMap<>();
    }

    public void handleMessage(String message) {
        System.out.println(message);

        String orderBookPair = getOrderBookPair(message);

        if (orderBookPair == null) {
            return;
        }

        OrderBook book = orderBookMap.get(orderBookPair);
        if (book == null) {
            orderBookMap.put(orderBookPair, buildOrderBook(message));
        } else {
            updateOrderBook(message, book);
            orderBookMap.put(orderBookPair, book);
        }

        AppConsolePrinter.display(orderBookMap);
    }
}
