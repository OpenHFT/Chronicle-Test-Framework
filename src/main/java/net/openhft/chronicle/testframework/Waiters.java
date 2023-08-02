package net.openhft.chronicle.testframework;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.openhft.chronicle.testframework.ThreadUtil.pause;

/**
 * A utility class providing methods to create wait conditions. These conditions pause the execution of the
 * program until certain criteria are met or until a specified amount of time has elapsed.
 * <p>
 * The class provides functionality for boolean as well as generic conditions and uses a builder pattern
 * for customization of the waiting behavior, such as the maximum time to wait and the interval to check
 * the condition.
 */
public class Waiters {

    // Default maximum time to wait for a condition in milliseconds
    private static final int DEFAULT_MAX_WAIT_TIME_MS = 5_000;
    // Default time interval to check the condition in milliseconds
    private static final int DEFAULT_CHECK_INTERVAL_MS = 10;
    // Default identity predicate for boolean conditions
    private static final Predicate<Boolean> IDENTITY = t -> t;
    // Default failure message for boolean conditions
    private static final String DEFAULT_BOOLEAN_MESSAGE = "Condition not met";
    // Default failure message for generic conditions, with placeholder for the last value
    private static final String DEFAULT_GENERIC_MESSAGE = "Condition not met, lastValue = %s";

    /**
     * Waits for the specified condition to be true, throwing a {@link ConditionNotMetException} if the condition is not met within the specified time.
     * <p>
     * This method is useful for synchronizing activities in concurrent programming, such as waiting for a connection to close.
     *
     * @param message         The message to include in the exception if the condition is not met.
     * @param condition       A supplier of the condition being tested, which returns true when the condition is met.
     * @param maxTimeToWaitMs The maximum amount of time to wait for the condition, in milliseconds.
     * @throws ConditionNotMetException If the condition is not met within the specified time.
     */
    public static void waitForCondition(String message, Supplier<Boolean> condition, long maxTimeToWaitMs) {
        builder(condition)
                .message(message)
                .maxTimeToWaitMs(maxTimeToWaitMs)
                .run();
    }

    /**
     * Creates a builder for waiting on a boolean condition.
     * <p>
     * This method simplifies the creation of waiters for boolean conditions.
     *
     * @param condition the condition, when it returns true the waiter will complete.
     * @return the {@link WaiterBuilder} for further configuration.
     */
    public static WaiterBuilder<Boolean> builder(Supplier<Boolean> condition) {
        return builder(condition, IDENTITY).message(DEFAULT_BOOLEAN_MESSAGE);
    }

    /**
     * Creates a builder for waiting on a generic condition, allowing customization of the condition testing.
     * <p>
     * This method provides more flexibility for defining complex waiting conditions.
     *
     * @param valueSupplier   The supplier of the value being tested, which will be regularly evaluated.
     * @param conditionTester A predicate that determines if the condition has been met based on the supplied value.
     * @param <T>             The type of the value being tested.
     * @return the {@link WaiterBuilder} for further configuration.
     */
    public static <T> WaiterBuilder<T> builder(Supplier<T> valueSupplier, Predicate<T> conditionTester) {
        return new WaiterBuilder<>(valueSupplier, conditionTester);
    }

    public static class WaiterBuilder<T> implements Runnable {
        // Supplier that provides a value
        private final Supplier<T> valueSupplier;

        // Predicate to test the provided value
        private final Predicate<T> conditionTester;

        // Maximum time to wait for a condition in milliseconds
        private long maxTimeToWaitMs = DEFAULT_MAX_WAIT_TIME_MS;

        // Time interval to check the condition in milliseconds
        private long checkIntervalMs = DEFAULT_CHECK_INTERVAL_MS;

        // Function to generate a message in case the condition isn't met
        private Function<T, String> messageGenerator = lastValue -> String.format(DEFAULT_GENERIC_MESSAGE, lastValue);

        // Constructor
        private WaiterBuilder(Supplier<T> valueSupplier, Predicate<T> conditionTester) {
            this.valueSupplier = valueSupplier;
            this.conditionTester = conditionTester;
        }

        /**
         * Wait for the condition to be true, throw an {@link ConditionNotMetException}
         * if the condition is not met in time.
         * <p>
         * This method is overridden from the Runnable interface, allowing the WaiterBuilder
         * to be run as a separate thread.
         */
        @Override
        public void run() {
            long endTime = System.currentTimeMillis() + maxTimeToWaitMs;
            while (true) {
                final T value = valueSupplier.get();
                if (conditionTester.test(value)) {
                    break; // Condition met, break the loop
                }
                if (System.currentTimeMillis() > endTime) {
                    // Condition wasn't met in the allowed time, throw an exception
                    throw new ConditionNotMetException(value, messageGenerator.apply(value));
                }
                // Condition wasn't met yet, pause before checking again
                pause(checkIntervalMs);
            }
        }

        /**
         * Specifies a static message for the exception in case the condition isn't met.
         *
         * @param message the message to be displayed when an exception is thrown.
         * @return the WaiterBuilder instance to allow for method chaining.
         */
        public WaiterBuilder<T> message(String message) {
            // Override the default message generator with a function that returns the provided message
            this.messageGenerator = lastValue -> message;
            return this; // Return the instance for method chaining
        }

        /**
         * Supply a function for generating the exception message in case the condition isn't met.
         *
         * @param messageGenerator a function that takes the last value as a parameter to create a message.
         * @return this WaiterBuilder instance to allow method chaining.
         */
        public WaiterBuilder<T> messageGenerator(Function<T, String> messageGenerator) {
            this.messageGenerator = messageGenerator;
            return this;
        }

        /**
         * Specify the maximum amount of time to wait for the condition to be met.
         *
         * @param maxTimeToWaitMs the maximum time in milliseconds to wait for the condition.
         * @return this WaiterBuilder instance to allow method chaining.
         */
        public WaiterBuilder<T> maxTimeToWaitMs(long maxTimeToWaitMs) {
            this.maxTimeToWaitMs = maxTimeToWaitMs;
            return this;
        }

        /**
         * Specify the interval between checks of the condition. This determines how often the condition is evaluated.
         *
         * @param checkIntervalMs the interval between checks in milliseconds.
         * @return this WaiterBuilder instance to allow method chaining.
         */
        public WaiterBuilder<T> checkIntervalMs(long checkIntervalMs) {
            this.checkIntervalMs = checkIntervalMs;
            return this;
        }
    }

    /**
     * An exception class that is thrown when a condition is not met within the specified time.
     */
    public static class ConditionNotMetException extends RuntimeException {
        private static final long serialVersionUID = 2827672436814649510L;
        private final Object lastValue;

        /**
         * Constructor for the exception.
         *
         * @param lastValue the last value that was evaluated when the exception was thrown
         * @param message   the message to be included with the exception
         */
        ConditionNotMetException(Object lastValue, String message) {
            super(message);
            this.lastValue = lastValue;
        }

        /**
         * Retrieve the last value that was evaluated when the exception was thrown.
         *
         * @return the last value
         */
        public Object lastValue() {
            return lastValue;
        }
    }
}
