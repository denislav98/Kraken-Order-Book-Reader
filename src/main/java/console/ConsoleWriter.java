package console;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;

import model.OrderBook;

public class ConsoleWriter {

    private static final String BEST_ASK_OR_BID_MSG_FORMAT = "best %s: [%s, %s]%n";
    private static final String ORDER_BOOK_ASKS = "asks";
    private static final String ORDER_BOOK_BIDS = "bids";

    /**
     * Simple console printing method to display the order book asks and bids for the given pair
     * @param orderBookMap a map of pair and corresponding order book
     */
    public static void display(Map<String, OrderBook> orderBookMap) {
        for (Map.Entry<String, OrderBook> orderBookEntry : orderBookMap.entrySet()) {
            OrderBook book = orderBookEntry.getValue();
            printBeggingMessage();
            printBookAsks(book);
            printBookBids(book);
            printOrderBookPair(orderBookEntry);
            printLocalDateTime();
            printEndMessage();
        }
    }

    private static void printLocalDateTime() {
        System.out.println(LocalDateTime.now());
    }

    private static void printOrderBookPair(Map.Entry<String, OrderBook> orderBookEntry) {
        System.out.println(orderBookEntry.getKey());
    }

    private static void printEndMessage() {
        System.out.println(">-------------------------------------<");
    }

    private static void printBeggingMessage() {
        System.out.println("<------------------------------------>");
    }

    private static void printBookAsks(OrderBook book) {
        SortedMap<Float, Float> asks = book.getAsks();
        System.out.println("asks:");
        printOrderBook(asks);
        printBestOrderBookAsks(asks);
    }

    private static void printBookBids(OrderBook book) {
        SortedMap<Float, Float> asks = book.getBids();
        System.out.println("bids:");
        printOrderBook(asks);
        printBestOrderBookBids(asks);
    }

    private static void printBestOrderBookAsks(SortedMap<Float, Float> map) {
        Map.Entry<Float, Float> bestAsk = map.entrySet().iterator().next();
        System.out.printf(BEST_ASK_OR_BID_MSG_FORMAT, ORDER_BOOK_ASKS, bestAsk.getKey(),
                bestAsk.getValue());
    }

    private static void printBestOrderBookBids(SortedMap<Float, Float> map) {
        float bestBidPrice = map.lastKey();
        float bestBidVolume = map.get(bestBidPrice);
        System.out.printf(BEST_ASK_OR_BID_MSG_FORMAT, ORDER_BOOK_BIDS, bestBidPrice, bestBidVolume);
    }

    private static void printOrderBook(Map<Float, Float> map) {
        for (Map.Entry<Float, Float> bid : map.entrySet()) {
            System.out.printf("[ %s, %s ]%n", bid.getKey(), bid.getValue());
        }
    }
}
