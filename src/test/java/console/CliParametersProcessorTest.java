package console;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static console.CliParametersProcessor.INVALID_PAIR_PROVIDED_ERROR_MSG;
import static console.CliParametersProcessor.NO_COMMAND_LINE_ARGS_PROVIDED_ERROR_MSG;

import java.util.List;

import org.junit.Test;

public class CliParametersProcessorTest {

    private final ICliParametersProcessor classUnderTest = new CliParametersProcessor();

    @Test
    public void givenValidOrderBookPairs_whenProcessingArguments_thenAssertArgumentsProcessedCorrectly() {
        String[] arguments = new String[] { "ETH/USD,BTC/USD" };

        List<String> processedArguments = classUnderTest.processArguments(arguments);

        assertThat(processedArguments.size(), is(2));
        assertThat(processedArguments.get(0), is("ETH/USD"));
        assertThat(processedArguments.get(1), is("BTC/USD"));
    }

    @Test
    public void givenEmptyConsoleLineArgs_whenProcessingArguments_thenExceptionIsThrown() {
        String[] arguments = new String[] {};

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.processArguments(arguments));

        assertThat(exception, instanceOf(IllegalArgumentException.class));
        assertThat(exception.getMessage(), is(NO_COMMAND_LINE_ARGS_PROVIDED_ERROR_MSG));
    }

    @Test
    public void givenInvalidOrderBookPair_whenProcessingArguments_thenExceptionIsThrown() {
        String[] arguments = new String[] { "ETT/USD" };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.processArguments(arguments));

        assertThat(exception, instanceOf(IllegalArgumentException.class));
        assertThat(exception.getMessage(),
                is(format(INVALID_PAIR_PROVIDED_ERROR_MSG, "ETT", "ETT/USD")));
    }
}
