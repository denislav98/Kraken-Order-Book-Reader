package websocket.message;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.OrderBook;
import model.OrderBookElement;

/**
 * Helper class to deal with JSON payload used in the CoinbaseProd WebSocket API requests and responses.
 * Parses json simple library objects into CoinbaseProReader POJOs.
 */
public class WebSocketMessagePayloadHelper {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketMessagePayloadHelper.class);

    private static final String ASKS = "as";
    private static final String ASKS_UPDATE = "a";
    private static final String BIDS = "bs";
    private static final String BIDS_UPDATE = "b";

    private static final Gson GSON = new Gson().newBuilder().create();

    /**
     * Helper method to build a String subscribe message for Kraken Websockets API.
     * @see  <a href="https://docs.kraken.com/websockets/#message-subscribe"/>
     * @param orderBookPairs - pair, e.g. "ETH-USD","BTC/USD"
     * @return String - String message
     *
     * <b>Example: </b> {"event": "subscribe", "pair": ["ETH-USD","BTC/USD"], "subscription": {"name": "book"} }
     */
    public static String createKrakenSubscribeMessage(List<String> orderBookPairs) {
        // Sanity check - fail-fast
        if (orderBookPairs == null || orderBookPairs.isEmpty()) {
            System.err.println("Order book pairs are required.");
            System.exit(1);
        }

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
    public static OrderBook buildOrderBook(String response) {
        // Sanity check - fail-fast
        if (response == null) {
            return null;
        }

        if (response.contains("event")) {
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
        if (response == null) {
            return;
        }

        if (response.contains("event")) {
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
        if (response == null) {
            return null;
        }

        if (response.contains("event")) {
            return null;
        }

        return GSON.fromJson(response, JsonArray.class).get(3).getAsString();
    }

    private static JsonObject getOrderBookJsonObject(String response) {
        JsonArray jsonArray = GSON.fromJson(response, JsonArray.class);
        return jsonArray.get(1).getAsJsonObject();
    }

    private static void updateOrderBookBids(JsonObject orderBookJsonObject, OrderBook book) {
        JsonArray bidsList = orderBookJsonObject.get(BIDS_UPDATE).getAsJsonArray();
        List<OrderBookElement> updatedElements = buildOrderBookUpdate(bidsList);
        book.updateBids(updatedElements);
    }

    private static void updateOrderBookAsks(JsonObject orderBookJsonObject, OrderBook book) {
        JsonArray asksList = orderBookJsonObject.get(ASKS_UPDATE).getAsJsonArray();
        List<OrderBookElement> updatedElements = buildOrderBookUpdate(asksList);
        book.updateAsks(updatedElements);
    }

    private static OrderBook buildOrderBook(JsonObject orderBook) {
        JsonArray asksList = orderBook.get(ASKS).getAsJsonArray();
        JsonArray bidsList = orderBook.get(BIDS).getAsJsonArray();

        Map<Float, Float> asks = extractPricePerVolumeEntries(asksList);
        Map<Float, Float> bids = extractPricePerVolumeEntries(bidsList);

        return new OrderBook(asks, bids);
    }

    /**
     * Helper method to extract all of price points, given an array of arrays (price, volume).
     * @param priceWithVolumeArray prices/volumes as JSONArray
     * @return Map of [price, volume] entries
     */
    private static Map<Float, Float> extractPricePerVolumeEntries(JsonArray priceWithVolumeArray) {
        // Sanity check - fail-fast
        if (priceWithVolumeArray == null || priceWithVolumeArray.size() == 0) {
            return emptyMap();
        }

        Map<Float, Float> prices = new TreeMap<>();

        for (JsonElement element : priceWithVolumeArray) {
            OrderBookElement orderBookElement = getOrderBookElement(element);
            prices.put(orderBookElement.getPrice(), orderBookElement.getVolume());
        }

        return prices;
    }

    /**
     * Given an array from Kraken update payload response in the form of (price, volume and date),
     * then build order book update elements and add them to a collection
     * @param jsonArray a collection of (price, volume, date) elements
     * @return List of OrderBookElement`s
     */
    private static List<OrderBookElement> buildOrderBookUpdate(JsonArray jsonArray) {
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
