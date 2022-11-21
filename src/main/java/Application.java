import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import console.CliParametersProcessor;
import console.ICliParametersProcessor;
import websocket.WebSocketClient;

public class Application {

    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    public static final String KRAKEN_WS_URI = "wss://ws.kraken.com/";

    public static void main(String[] args) {
        try {
            ICliParametersProcessor parametersProcessor = new CliParametersProcessor();
            List<String> orderBookPairs = parametersProcessor.processArguments(args);
            /////////////////////////////////////////////////
            // Connect to Kraken WebSocket API and do the processing
            /////////////////////////////////////////////////
            WebSocketClient wsc = new WebSocketClient(KRAKEN_WS_URI);
            wsc.subscribe(orderBookPairs);
            // Block in wait state till unlocked by pressing Control-c
            createCountDownLatch().await();
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }
    }

    private static CountDownLatch createCountDownLatch() {
        // Create barrier and set countdown counter to 1
        CountDownLatch doneSignal = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(createShutDownHook(doneSignal));
        return doneSignal;
    }

    private static Thread createShutDownHook(CountDownLatch doneSignal) {
        return new Thread() {
            /**
             * Callback for Control-c
             */
            @Override
            public void run() {
                // Unlock the latch with main thread awaiting for it, will continue and do the clean up
                doneSignal.countDown();
            }
        };
    }
}
