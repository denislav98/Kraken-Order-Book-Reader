package websocket;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import websocket.message.IMessageHandler;

public class WebSocketClientTest {

    private static final String TEST_ORDER_BOOK_PAIR = "ETH/USD";
    private static final String TEST_SUBSCRIPTION_MSG = "{\"event\":\"subscribe\",\"pair\":[\"%s\"],\"subscription\":{\"name\":\"book\"}}";

    private final IMessageHandler mockMsgHandler = mock(IMessageHandler.class);
    private final WebSocketClientEndpoint mockEndpoint = mock(WebSocketClientEndpoint.class);

    @Test
    public void givenValidOrderBookPairs_whenSubscribe_thenAssertValidSubscriptionCreated() {
        WebSocketClient classUnderTest = new WebSocketClient(mockEndpoint, mockMsgHandler);

        classUnderTest.subscribe(singletonList(TEST_ORDER_BOOK_PAIR));

        verify(mockEndpoint).sendMessage(eq(format(TEST_SUBSCRIPTION_MSG, TEST_ORDER_BOOK_PAIR)));
        verify(mockEndpoint).addMessageHandler(eq(mockMsgHandler));
    }
}
