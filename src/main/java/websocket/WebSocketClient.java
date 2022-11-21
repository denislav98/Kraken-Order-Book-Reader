package websocket;

import static websocket.message.WebSocketMessagePayloadHelper.createSubscribeMessage;

import java.util.List;

import websocket.message.IMessageHandler;
import websocket.message.WebSocketMessageHandler;

public class WebSocketClient {

    private final WebSocketClientEndpoint clientEndPoint;
    private final IMessageHandler messageHandler;

    public WebSocketClient(String webSocketWsUri) {
        // Establish WebSocket connection
        this(new WebSocketClientEndpoint(webSocketWsUri), new WebSocketMessageHandler());
    }

    WebSocketClient(WebSocketClientEndpoint clientEndPoint, IMessageHandler messageHandler) {
        this.clientEndPoint = clientEndPoint;
        this.messageHandler = messageHandler;
    }

    public void subscribe(List<String> orderBookPairs) {
        // Inject messages handler
        clientEndPoint.addMessageHandler(messageHandler);
        // Create a subscription message
        String subscribeMessage = createSubscribeMessage(orderBookPairs);
        // Send message subscription and thus start listening on responses
        clientEndPoint.sendMessage(subscribeMessage);
    }
}
