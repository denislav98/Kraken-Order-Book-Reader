package websocket.message;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import model.OrderBook;
import model.OrderBookElement;

/**
 * Helper class to deal with JSON payload used in the CoinbaseProd WebSocket API requests and responses.
 * Parses json simple library objects into CoinbaseProReader POJOs.
 */
public class WebSocketMessagePayloadHelper {

    private static final String ASKS = "as";
    private static final String ASKS_UPDATE = "a";
    private static final String BIDS = "bs";
    private static final String BIDS_UPDATE = "b";
    private static final int ORDER_BOOK_PAIR_INDEX_FROM_RESPONSE = 3;

    private static final Gson GSON = new Gson().newBuilder().create();

    /**
     * Helper method to build a String subscribe message for Kraken Websockets API.
     * @see  <a href="https://docs.kraken.com/websockets/#message-subscribe"/>
     * @param orderBookPairs - pair, e.g. "ETH-USD","BTC/USD"
     * @return String - String message
     *
     * <b>Example: </b> {"event": "subscribe", "pair": ["ETH/USD","BTC/USD"], "subscription": {"name": "book"} }
     */
    public static String createSubscribeMessage(List<String> orderBookPairs) {
        JsonObject simpleSubscribeMessage = new JsonObject();
        simpleSubscribeMessage.add("event", new JsonPrimitive("subscribe"));

        JsonArray pairs = new JsonArray();
        for (String pair : orderBookPairs) {
            pairs.add(pair);
        }
        simpleSubscribeMessage.add("pair", pairs);

        JsonObject subscription = new JsonObject();
        subscription.add("name", new JsonPrimitive("book"));

        simpleSubscribeMessage.add("subscription", subscription);

        return simpleSubscribeMessage.toString();
    }

    /**
     * Helper method to build the Order Book from Kraken WebSocket API response message.
     * @param response String response message
     * @return OrderBook object build from the response
     */
    public static OrderBook createOrderBook(String response) {
        // Sanity check - fail-fast
        if (isEmpty(response)) {
            return null;
        }

        JsonObject orderBookJsonObject = getOrderBookJsonObject(response);

        return buildOrderBook(orderBookJsonObject);
    }

    /**
     * Helper method to update the order book from Kraken WebSocket API response message.
     * @param response String response message
     * @param book - representing the order book.
     */
    public static void updateOrderBook(String response, OrderBook book) {
        // Sanity check - fail-fast
        if (isEmpty(response)) {
            return;
        }

        JsonObject orderBookJsonObject = getOrderBookJsonObject(response);

        if (response.contains(ASKS_UPDATE)) {
            updateOrderBookAsks(orderBookJsonObject, book);
        } else if (response.contains(BIDS_UPDATE)) {
            updateOrderBookBids(orderBookJsonObject, book);
        }
    }

    /**
     * Helper method to get the order book pair e.g BTC/USD from Kraken Kraken WebSocket API response message.
     * @param response - String response message
     * @return String value representing the order book pair
     */
    public static String getOrderBookPair(String response) {
        // Sanity check - fail-fast
        if (isEmpty(response)) {
            return EMPTY;
        }

        if (response.contains("event")) {
            return EMPTY;
        }

        return GSON.fromJson(response, JsonArray.class).get(ORDER_BOOK_PAIR_INDEX_FROM_RESPONSE)
                .getAsString();
    }

    private static JsonObject getOrderBookJsonObject(String response) {
        JsonArray jsonArray = GSON.fromJson(response, JsonArray.class);
        return jsonArray.get(1).getAsJsonObject();
    }

    private static void updateOrderBookBids(JsonObject orderBookJsonObject, OrderBook book) {
        JsonArray bidsList = orderBookJsonObject.get(BIDS_UPDATE).getAsJsonArray();
        List<OrderBookElement> updatedElements = getOrderBookUpdateElements(bidsList);
        book.updateBids(updatedElements);
    }

    private static void updateOrderBookAsks(JsonObject orderBookJsonObject, OrderBook book) {
        JsonArray asksList = orderBookJsonObject.get(ASKS_UPDATE).getAsJsonArray();
        List<OrderBookElement> updatedElements = getOrderBookUpdateElements(asksList);
        book.updateAsks(updatedElements);
    }

    private static OrderBook buildOrderBook(JsonObject orderBook) {
        JsonArray asksList = orderBook.get(ASKS).getAsJsonArray();
        JsonArray bidsList = orderBook.get(BIDS).getAsJsonArray();

        Map<Float, Float> asks = extractVolumePerPriceEntries(asksList);
        Map<Float, Float> bids = extractVolumePerPriceEntries(bidsList);

        return new OrderBook(asks, bids);
    }

    /**
     * Helper method to extract all of price points, given an array of arrays (price, volume).
     * @param priceWithVolumeArray prices/volumes as JSONArray
     * @return Map of [price, volume] entries
     */
    private static Map<Float, Float> extractVolumePerPriceEntries(JsonArray priceWithVolumeArray) {
        Map<Float, Float> volumePerPrice = new TreeMap<>();

        for (JsonElement element : priceWithVolumeArray) {
            OrderBookElement orderBookElement = getOrderBookElement(element);
            volumePerPrice.put(orderBookElement.getPrice(), orderBookElement.getVolume());
        }

        return volumePerPrice;
    }

    /**
     * Given an array from Kraken update payload response in the form of (price, volume and date),
     * then build order book update elements and add them to a collection
     * @param jsonArray a collection of (price, volume, date) elements
     * @return List of OrderBookElement`s
     */
    private static List<OrderBookElement> getOrderBookUpdateElements(JsonArray jsonArray) {
        // Sanity check - fail-fast
        if (jsonArray == null || jsonArray.size() == 0) {
            return emptyList();
        }

        List<OrderBookElement> updates = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            updates.add(getOrderBookElement(element));
        }

        return updates;
    }

    private static OrderBookElement getOrderBookElement(JsonElement element) {
        JsonArray orderBookArray = element.getAsJsonArray();
        Float price = orderBookArray.get(0).getAsFloat();
        Float volume = orderBookArray.get(1).getAsFloat();
        return new OrderBookElement(price, volume);
    }
}
