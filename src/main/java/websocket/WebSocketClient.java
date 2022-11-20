package websocket;

import static websocket.message.WebSocketMessagePayloadHelper.createKrakenSubscribeMessage;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import websocket.message.IMessageHandler;
import websocket.message.WebSocketMessageHandler;

public class WebSocketClient {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketClient.class);

    private static final String KRAKEN_WS_URI = "wss://ws.kraken.com/";

    private WebSocketClientEndpoint clientEndPoint;
    private final IMessageHandler messageHandler;

    public WebSocketClient() {
        this.messageHandler = new WebSocketMessageHandler();
    }

    WebSocketClient(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void execute(List<String> orderBookPairs) {
        try {
            // Establish WebSocket connection
            clientEndPoint = new WebSocketClientEndpoint(new URI(KRAKEN_WS_URI));
            // Inject messages handler
            clientEndPoint.addMessageHandler(messageHandler);
            // Create a subscription message
            String subscribeMessage = createKrakenSubscribeMessage(orderBookPairs);
            // Send message subscription and thus start listening on responses
            clientEndPoint.sendMessage(subscribeMessage);
        } catch (Exception e) {
            LOGGER.error("Failed to establish WebSocket connection.", e);
        }
    }

    public WebSocketClientEndpoint getClientEndPoint() {
        return clientEndPoint;
    }
}
