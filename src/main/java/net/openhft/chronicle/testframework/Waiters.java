package net.openhft.chronicle.testframework;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Waiters {

    private static final int DEFAULT_MAX_WAIT_TIME_MS = 5_000;
    private static final int DEFAULT_CHECK_INTERVAL_MS = 10;
    private static final Predicate<Boolean> IDENTITY = t -> t;
    private static final String DEFAULT_BOOLEAN_MESSAGE = "Condition not met";
    private static final String DEFAULT_GENERIC_MESSAGE = "Condition not met, lastValue = %s";

    /**
     * Wait for a condition to be true, throw an {@link ConditionNotMetException} if the condition is not met in time
     *
     * @param condition       The condition to test
     * @param maxTimeToWaitMs The maximum amount of time to wait before failing
     */
    public static void waitForCondition(String message, Supplier<Boolean> condition, long maxTimeToWaitMs) {
        builder(condition)
                .message(message)
                .maxTimeToWaitMs(maxTimeToWaitMs)
                .run();
    }

    /**
     * Create a builder that will succeed when the supplied condition is met
     *
     * @param condition the condition, when it returns true the waiter will complete
     * @return the {@link WaiterBuilder}
     */
    public static WaiterBuilder<Boolean> builder(Supplier<Boolean> condition) {
        return builder(condition, IDENTITY).message(DEFAULT_BOOLEAN_MESSAGE);
    }

    /**
     * Create a Waiter that will succeed when the value matches the predicate
     *
     * @param valueSupplier   the supplier of the value, regularly evaluated
     * @param conditionTester the predicate that determines if the condition has been met
     * @param <T>             the value being tested
     * @return the {@link WaiterBuilder}
     */
    public static <T> WaiterBuilder<T> builder(Supplier<T> valueSupplier, Predicate<T> conditionTester) {
        return new WaiterBuilder<>(valueSupplier, conditionTester);
    }

    public static class WaiterBuilder<T> implements Runnable {
        private final Supplier<T> valueSupplier;
        private final Predicate<T> conditionTester;
        private long maxTimeToWaitMs = DEFAULT_MAX_WAIT_TIME_MS;
        private long checkIntervalMs = DEFAULT_CHECK_INTERVAL_MS;
        private Function<T, String> messageGenerator = lastValue -> String.format(DEFAULT_GENERIC_MESSAGE, lastValue);

        private WaiterBuilder(Supplier<T> valueSupplier, Predicate<T> conditionTester) {
            this.valueSupplier = valueSupplier;
            this.conditionTester = conditionTester;
        }

        /**
         * Wait for the condition to be true, throw an {@link ConditionNotMetException} if the condition is not met in time
         */
        @Override
        public void run() {
            long endTime = System.currentTimeMillis() + maxTimeToWaitMs;
            while (true) {
                final T value = valueSupplier.get();
                if (conditionTester.test(value)) {
                    break;
                }
                if (System.currentTimeMillis() > endTime) {
                    throw new ConditionNotMetException(value, messageGenerator.apply(value));
                }
                pause(checkIntervalMs);
            }
        }

        /**
         * Specify a static message for the exception
         *
         * @param message the message
         * @return this
         */
        public WaiterBuilder<T> message(String message) {
            this.messageGenerator = lastValue -> message;
            return this;
        }

        /**
         * Supply a function for generating the exception message
         *
         * @param messageGenerator a function that takes the last value as a parameter
         * @return this
         */
        public WaiterBuilder<T> messageGenerator(Function<T, String> messageGenerator) {
            this.messageGenerator = messageGenerator;
            return this;
        }

        /**
         * Specify the maximum amount of time to wait for the condition to be met
         *
         * @param maxTimeToWaitMs the maximum time in milliseconds
         * @return this
         */
        public WaiterBuilder<T> maxTimeToWaitMs(long maxTimeToWaitMs) {
            this.maxTimeToWaitMs = maxTimeToWaitMs;
            return this;
        }

        /**
         * Specify the interval between checks
         *
         * @param checkIntervalMs the interval between checks in milliseconds
         * @return this
         */
        public WaiterBuilder<T> checkIntervalMs(long checkIntervalMs) {
            this.checkIntervalMs = checkIntervalMs;
            return this;
        }

        private void pause(long timeInMillis) {
            try {
                Thread.sleep(timeInMillis);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    public static class ConditionNotMetException extends RuntimeException {
        private static final long serialVersionUID = 2827672436814649510L;
        private final Object lastValue;

        ConditionNotMetException(Object lastValue, String message) {
            super(message);
            this.lastValue = lastValue;
        }

        public Object lastValue() {
            return lastValue;
        }
    }
}
