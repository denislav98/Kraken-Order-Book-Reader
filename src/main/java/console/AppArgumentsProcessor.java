package console;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Currency;

public class AppArgumentsProcessor {

    private static final Logger LOGGER = LogManager.getLogger(AppArgumentsProcessor.class);

    public static List<String> processArguments(String[] args) {
        assertNonEmptyArguments(args);
        String[] orderBookPairs = args[0].split(",");
        List<String> validPairs = new ArrayList<>();
        for (String pair : orderBookPairs) {
            assertValidOrderBookPair(pair);
            validPairs.add(pair);
        }
        return validPairs;
    }

    private static void assertValidOrderBookPair(String orderBookPair) {
        String[] orderBookPairParts = orderBookPair.split("/");
        assertValidCurrencyPartOfPair(orderBookPair, orderBookPairParts[0]);
        assertValidCurrencyPartOfPair(orderBookPair, orderBookPairParts[1]);
    }

    private static void assertValidCurrencyPartOfPair(String orderBookPair, String firstCurrency) {
        if (!isValidEnum(Currency.class, firstCurrency)) {
            LOGGER.error(format("Invalid currency: '%s' provided part of order book pair: '%s'",
                    firstCurrency, orderBookPair));
            System.exit(1);
        }
    }

    private static void assertNonEmptyArguments(String[] args) {
        if (isEmpty(args)) {
            LOGGER.error(
                    "No command line arguments are provided. Expecting: BTC/USD or ETH/USD. Exiting...");
            System.exit(1);
        }
    }
}
