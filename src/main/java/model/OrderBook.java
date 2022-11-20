package model;

import static java.util.Collections.reverseOrder;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class to hold order book and perform relevant operations such as add/update/remove asks and bids.
 */
public class OrderBook {

    private final SortedMap<Float, Float> asks;
    private final SortedMap<Float, Float> bids;

    public OrderBook(Map<Float, Float> asks, Map<Float, Float> bids) {
        //from highest ask to lowest bid
        this.asks = new TreeMap<>(reverseOrder());
        this.bids = new TreeMap<>();
        this.asks.putAll(asks);
        this.bids.putAll(bids);
    }

    /**
     * Update order book asks with a given list of (price, volume) combos.
     * @param asksUpdate - represents a list of updated order book elements
     * Note: As per Kraken WebSocket API - a volume 0 means removal.
     * @see  <a href="https://support.kraken.com/hc/en-us/articles/360027821131-How-to-maintain-a-valid-order-book"/>
     */
    public void updateAsks(List<OrderBookElement> asksUpdate) {
        for (OrderBookElement orderBookElement : asksUpdate) {
            if (orderBookElement.getVolume() == 0) {
                // remove a price point due to size being 0
                asks.remove(orderBookElement.getPrice());
            } else {
                // insert the new price point according to the natural order
                asks.put(orderBookElement.getPrice(), orderBookElement.getVolume());
            }
        }
    }

    /**
     * Update order book bids with a given list of (price, volume) combos.
     * @param bidsUpdate - represents a list of updated order book elements
     * Note: As per Kraken WebSocket API - a volume 0 means removal.
     * @see  <a href="https://support.kraken.com/hc/en-us/articles/360027821131-How-to-maintain-a-valid-order-book"/>
     */
    public void updateBids(List<OrderBookElement> bidsUpdate) {
        for (OrderBookElement orderBookElement : bidsUpdate) {
            if (orderBookElement.getVolume() == 0) {
                // remove a price point due to size being 0
                bids.remove(orderBookElement.getPrice());
            } else {
                // insert the new price point according to the natural order
                bids.put(orderBookElement.getPrice(), orderBookElement.getVolume());
            }
        }
    }

    public SortedMap<Float, Float> getAsks() {
        return asks;
    }

    public SortedMap<Float, Float> getBids() {
        return bids;
    }
}
