package websocket;

public class WebSocketClientConnectionException extends RuntimeException {

    public WebSocketClientConnectionException(String msg, Throwable t) {
        super(msg, t);
    }
}
