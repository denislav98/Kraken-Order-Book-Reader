import static java.util.Arrays.asList;
import static console.AppArgumentsProcessor.processArguments;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import websocket.WebSocketClient;

public class Application {

    public static void main(String[] args) {
        // Create barrier and set countdown counter to 1
        CountDownLatch doneSignal = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            /**
             * Callback for Control-c
             */
            @Override
            public void run() {
                // Unlock the latch with main thread awaiting for it, will continue and do the clean up
                doneSignal.countDown();
            }
        });

        /////////////////////////////////////////////////
        // Connect to WebSocket API and do the processing
        /////////////////////////////////////////////////
        List<String> orderBookPairs = asList("BTC/USD","ETH/USD");//processArguments(args);
        // asList("BTC/USD","ETH/USD"); //Application.processInput(args);
        WebSocketClient wsc = new WebSocketClient();
        wsc.execute(orderBookPairs);

        // Block in wait state till unlocked by pressing Control-c
        try {
            doneSignal.await();
            wsc.getClientEndPoint().close();
        } catch (InterruptedException e) {

        }

        System.out.println("Exiting...");
    }
}
