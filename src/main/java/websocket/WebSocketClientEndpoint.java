package websocket;

import static java.lang.String.format;
import static console.ConsoleWriter.display;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.OrderBook;
import websocket.message.IMessageHandler;

@ClientEndpoint()
public class WebSocketClientEndpoint {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketClientEndpoint.class);

    private Session userSession;
    private IMessageHandler messageHandler;

    public WebSocketClientEndpoint(String endpoint) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            userSession = container.connectToServer(this, new URI(endpoint));
        } catch (Exception e) {
            String errorMsg = format("Failed to connect to server endpoint %s", endpoint);
            LOGGER.error(format(errorMsg, endpoint), e);
            throw new WebSocketClientConnectionException(errorMsg, e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        // Callback to be called only once upon a new connection
        LOGGER.info("Opening WebSocket");
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(CloseReason reason) {
        LOGGER.info("onClose: " + reason.toString());
        if (userSession != null) {
            try {
                userSession.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close connection", e);
            }
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            Map<String, OrderBook> orderBookMap = this.messageHandler.handleMessage(message);
            display(orderBookMap);
        }
    }

    @OnError
    public void onError(Session session, Throwable ex) {
        LOGGER.error(format("WebSocket error => '%s' => '%s'", session, ex.getMessage()), ex);
    }

    public void addMessageHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }
}