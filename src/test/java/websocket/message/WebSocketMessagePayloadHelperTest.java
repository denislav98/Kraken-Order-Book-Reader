package websocket.message;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static websocket.message.WebSocketMessagePayloadHelper.createOrderBook;
import static websocket.message.WebSocketMessagePayloadHelper.createSubscribeMessage;
import static websocket.message.WebSocketMessagePayloadHelper.getOrderBookPair;
import static websocket.message.WebSocketMessagePayloadHelper.updateOrderBook;

import java.util.Map;

import org.junit.Test;

import model.OrderBook;

public class WebSocketMessagePayloadHelperTest {

    private static final String ETH_TO_USD = "ETH/USD";

    private static final float TEST_ASK_PRICE = 16.10f;
    private static final float TEST_ASK_VOLUME = 6.30f;
    private static final float TEST_BID_PRICE = 16.000f;
    private static final float TEST_BID_VOLUME = 0.007f;

    private static final float TEST_UPDATE_ASK_PRICE = 16063.20000f;
    private static final float TEST_UPDATE_ASK_VOLUME_OLD = 1.31f;
    private static final float TEST_UPDATE_ASK_VOLUME_NEW = 1.87f;
    private static final float TEST_DELETE_ASK_PRICE = 16059.40000f;
    private static final float TEST_DELETE_ASK_VOLUME_OLD = 1.4356f;
    private static final float TEST_UPDATE_BID_PRICE = 1500.20000f;
    private static final float TEST_UPDATE_BID_VOLUME = 1.40f;

    private static final String TEST_SUBSCRIPTION_MSG = "{\"event\":\"subscribe\",\"pair\":[\"%s\"],\"subscription\":{\"name\":\"book\"}}";
    private static final String TEST_CREATE_ORDER_BOOK_RESPONSE = "[336,{\"as\":[[\"%s\",\"%s\",\"1669028780.983665\"]],\"bs\":[[\"%s\",\"%s\",\"1669028775.666380\"]]},\"book-10\",\"ETH/USD\"]";
    private static final String TEST_UPDATE_ORDER_BOOK_ASKS_RESPONSE = "[336, { \"a\":[[\"16059.40000\", \"0.00000000\", \"1669031634.946619\"],[\"%s\", \"%s\", \"1669031634.050850\", \"r\"]],\"c\":\"2867552989\"},\"book-10\", \"XBT/USD\"]";
    private static final String TEST_UPDATE_ORDER_BOOK_BIDS_RESPONSE = "[336, { \"b\":[[\"16059.40000\", \"0.00000000\", \"1669031634.946619\"],[\"%s\", \"%s\", \"1669031634.050850\", \"r\"]],\"c\":\"2867552989\"},\"book-10\", \"XBT/USD\"]";
    private static final String TEST_RESPONSE_WITH_EVENT = "{\"connectionID\":8563586709029910710,\"event\":\"systemStatus\",\"status\":\"online\",\"version\":\"1.9.0\"}";

    @Test
    public void whenCreateSubscribeMessage_thenAssertCorrectMessageCreated() {
        assertThat(createSubscribeMessage(singletonList(ETH_TO_USD)),
                is(format(TEST_SUBSCRIPTION_MSG, ETH_TO_USD)));
    }

    @Test
    public void givenValidOrderBookJsonResponse_whenCreateOrderBook_thenAssertValidOrderBookCreated() {
        String response = prepareCreateOrderBookJsonResponse();

        OrderBook book = createOrderBook(response);

        Map<Float, Float> asks = requireNonNull(book).getAsks();
        assertThat(asks.size(), is(1));
        assertThat(asks.get(TEST_ASK_PRICE), is(TEST_ASK_VOLUME));

        Map<Float, Float> bids = book.getBids();
        assertThat(bids.size(), is(1));
        assertThat(bids.get(TEST_BID_PRICE), is(TEST_BID_VOLUME));
    }

    @Test
    public void givenEmptyJsonResponse_whenCreateOrderBook_thenAssertValidOrderBookCreated() {
        assertThat(createOrderBook(null), nullValue());
    }

    @Test
    public void givenAsksUpdateJsonResponse_whenUpdateOrderBook_thenAssertOrderBookAsksUpdated() {
        String response = prepareUpdateOrderBookAsksJsonResponse();
        OrderBook book = new OrderBook(prepareAsks(), prepareBids());

        assertThat(book.getAsks().size(), is(2));
        assertThat(book.getAsks().get(TEST_UPDATE_ASK_PRICE), is(TEST_UPDATE_ASK_VOLUME_OLD));
        assertThat(book.getAsks().get(TEST_DELETE_ASK_PRICE), is(TEST_DELETE_ASK_VOLUME_OLD));

        updateOrderBook(response, book);

        Map<Float, Float> asks = requireNonNull(book).getAsks();
        assertThat(asks.size(), is(1));
        assertThat(asks.get(TEST_UPDATE_ASK_PRICE), is(TEST_UPDATE_ASK_VOLUME_NEW));
        assertThat(asks.get(TEST_DELETE_ASK_PRICE), nullValue());

        Map<Float, Float> bids = book.getBids();
        assertThat(bids.size(), is(1));
        assertThat(bids.get(TEST_UPDATE_BID_PRICE), is(TEST_UPDATE_BID_VOLUME));
    }

    @Test
    public void givenBidsUpdateJsonResponse_whenUpdateOrderBook_thenAssertOrderBookBidsUpdated() {
        String response = prepareUpdateOrderBookBidsJsonResponse();
        OrderBook book = new OrderBook(prepareBids(), prepareAsks());

        assertThat(book.getBids().size(), is(2));
        assertThat(book.getBids().get(TEST_UPDATE_ASK_PRICE), is(TEST_UPDATE_ASK_VOLUME_OLD));
        assertThat(book.getBids().get(TEST_DELETE_ASK_PRICE), is(TEST_DELETE_ASK_VOLUME_OLD));

        updateOrderBook(response, book);

        Map<Float, Float> bids = requireNonNull(book).getBids();
        assertThat(bids.size(), is(1));
        assertThat(bids.get(TEST_UPDATE_ASK_PRICE), is(TEST_UPDATE_ASK_VOLUME_NEW));
        assertThat(bids.get(TEST_DELETE_ASK_PRICE), nullValue());

        Map<Float, Float> asks = book.getAsks();
        assertThat(asks.size(), is(1));
        assertThat(asks.get(TEST_UPDATE_BID_PRICE), is(TEST_UPDATE_BID_VOLUME));
    }

    @Test
    public void givenEmptyJsonResponse_whenUpdateOrderBook_thenAssertNothingIsUpdated() {
        OrderBook book = new OrderBook(prepareAsks(), prepareBids());

        updateOrderBook(EMPTY, book);

        Map<Float, Float> asks = requireNonNull(book).getAsks();
        assertThat(asks.size(), is(2));
        assertThat(asks.get(TEST_UPDATE_ASK_PRICE), is(TEST_UPDATE_ASK_VOLUME_OLD));
        assertThat(asks.get(TEST_DELETE_ASK_PRICE), is(TEST_DELETE_ASK_VOLUME_OLD));

        Map<Float, Float> bids = book.getBids();
        assertThat(bids.size(), is(1));
        assertThat(bids.get(TEST_UPDATE_BID_PRICE), is(TEST_UPDATE_BID_VOLUME));
    }

    @Test
    public void givenResponseContainingOrderBookPair_whenGetOrderBookPair_thenAssertValidPair() {
        String response = prepareCreateOrderBookJsonResponse();

        assertThat(getOrderBookPair(response), is("ETH/USD"));
    }

    @Test
    public void givenEmptyResponse_whenGetOrderBookPair_thenEmptyPairIsReturned() {
        assertThat(getOrderBookPair(EMPTY), is(EMPTY));
    }

    @Test
    public void givenResponseContainingEvent_whenGetOrderBookPair_thenEmptyPairIsReturned() {
        assertThat(getOrderBookPair(TEST_RESPONSE_WITH_EVENT), is(EMPTY));
    }

    private Map<Float, Float> prepareBids() {
        return Map.of(TEST_UPDATE_BID_PRICE, TEST_UPDATE_BID_VOLUME);
    }

    private Map<Float, Float> prepareAsks() {
        return Map.of(TEST_DELETE_ASK_PRICE, TEST_DELETE_ASK_VOLUME_OLD,
                TEST_UPDATE_ASK_PRICE, TEST_UPDATE_ASK_VOLUME_OLD);
    }

    private String prepareCreateOrderBookJsonResponse() {
        return format(TEST_CREATE_ORDER_BOOK_RESPONSE, TEST_ASK_PRICE,
                TEST_ASK_VOLUME, TEST_BID_PRICE, TEST_BID_VOLUME);
    }

    private String prepareUpdateOrderBookAsksJsonResponse() {
        return format(TEST_UPDATE_ORDER_BOOK_ASKS_RESPONSE, TEST_UPDATE_ASK_PRICE,
                TEST_UPDATE_ASK_VOLUME_NEW);
    }

    private String prepareUpdateOrderBookBidsJsonResponse() {
        return format(TEST_UPDATE_ORDER_BOOK_BIDS_RESPONSE, TEST_UPDATE_ASK_PRICE,
                TEST_UPDATE_ASK_VOLUME_NEW);
    }
}