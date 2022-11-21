package console;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Currency;

public class CliParametersProcessor implements ICliParametersProcessor {

    private static final Logger LOGGER = LogManager.getLogger(CliParametersProcessor.class);

    private static final String COMMA = ",";
    private static final String BACK_SLASH = "/";

    static final String INVALID_PAIR_PROVIDED_ERROR_MSG = "Invalid currency: '%s' provided part of order book pair: '%s'";
    static final String NO_COMMAND_LINE_ARGS_PROVIDED_ERROR_MSG = "No command line arguments are provided. Expecting: BTC/USD or ETH/USD. Exiting...";

    @Override
    public List<String> processArguments(String[] args) {
        assertNonEmptyArguments(args);
        String[] orderBookPairs = args[0].split(COMMA);
        List<String> validPairs = new ArrayList<>();
        for (String pair : orderBookPairs) {
            assertValidOrderBookPair(pair);
            validPairs.add(pair);
        }
        return validPairs;
    }

    private static void assertValidOrderBookPair(String orderBookPair) {
        String[] orderBookPairParts = orderBookPair.split(BACK_SLASH);
        assertValidCurrencyPartOfPair(orderBookPair, orderBookPairParts[0]);
        assertValidCurrencyPartOfPair(orderBookPair, orderBookPairParts[1]);
    }

    private static void assertValidCurrencyPartOfPair(String orderBookPair, String firstCurrency) {
        if (!isValidEnum(Currency.class, firstCurrency)) {
            String msg = format(INVALID_PAIR_PROVIDED_ERROR_MSG, firstCurrency, orderBookPair);
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    private static void assertNonEmptyArguments(String[] args) {
        if (isEmpty(args)) {
            LOGGER.error(NO_COMMAND_LINE_ARGS_PROVIDED_ERROR_MSG);
            throw new IllegalArgumentException(NO_COMMAND_LINE_ARGS_PROVIDED_ERROR_MSG);
        }
    }
}
