package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.VanillaFlakyTestRunnerBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Provides a runner for handling flaky tests, allowing configuration of various options like
 * retry count, delay between iterations, and logging.
 * <p>
 * This class is part of a builder pattern allowing users to define how flaky tests should be
 * handled, enabling them to configure and adapt the behavior as needed.
 */
public final class FlakyTestRunner {

    // Suppresses default constructor, ensuring non-instantiability.
    private FlakyTestRunner() {
    }

    /**
     * A functional interface representing a runnable action that might throw a specific exception.
     *
     * @param <T> The type of exception that the action might throw
     */
    @FunctionalInterface
    public interface RunnableThrows<T extends Throwable> {

        /**
         * Performs an action.
         *
         * @throws T if an underlying exception is thrown
         */
        void run() throws T;

        /**
         * Performs the action wrapping any thrown Throwable in an IllegalStateException.
         *
         * @throws IllegalStateException if an underlying Throwable is thrown
         */
        default void runOrThrow() {
            runOrThrow(IllegalStateException::new);
        }

        /**
         * Performs the action wrapping any thrown Throwable using the provided {@code exceptionMapper}.
         * This allows customization of how exceptions should be handled and converted.
         *
         * @param exceptionMapper A function mapping a caught Throwable to a RuntimeException
         * @throws RuntimeException if an underlying Throwable is thrown
         */
        default void runOrThrow(Function<? super Throwable, ? extends RuntimeException> exceptionMapper) {
            requireNonNull(exceptionMapper);
            try {
                run();
            } catch (Throwable x) {
                throw exceptionMapper.apply(x);
            }
        }
    }

    /**
     * Creates a builder for constructing a flaky test runner for an action that might throw checked exceptions.
     * <p>
     * The flaky test runner can be customized to handle various testing scenarios where a test may fail intermittently.
     *
     * @param action Action to perform, potentially throwing checked exceptions
     * @param <X>    Type of exception that can be thrown
     * @return A new builder for configuring the flaky test runner
     */
    public static <X extends Throwable> Builder<X> builder(@NotNull final RunnableThrows<X> action) {
        requireNonNull(action);
        return new VanillaFlakyTestRunnerBuilder<>(action);
    }

    /**
     * Creates a builder for constructing a flaky test runner for an action that might throw unchecked exceptions.
     * <p>
     * Similar to {@link #builder(RunnableThrows)}, but specific to unchecked exceptions.
     *
     * @param action Action to perform, potentially throwing unchecked exceptions
     * @return A new builder for configuring the flaky test runner
     */
    public static Builder<RuntimeException> builderUnchecked(@NotNull final Runnable action) {
        requireNonNull(action);
        return new VanillaFlakyTestRunnerBuilder<>(asRunnableThrows(action));
    }

    private static RunnableThrows<RuntimeException> asRunnableThrows(Runnable runnable) {
        return runnable::run;
    }
    /**
     * Defines the interface for constructing and configuring a flaky test runner.
     * <p>
     * A flaky test runner is used to handle test cases that might fail intermittently.
     * The builder allows for configuring various aspects of the runner such as the maximum number
     * of iterations before failure, the delay between iterations, garbage collection between
     * iterations, and custom loggers for informational and error messages.
     * <p>
     * Example usage:
     * <pre>
     * Builder&lt;Exception&gt; builder = FlakyTestRunner.builder(action);
     * builder.withMaxIterations(3)
     *        .withIterationDelay(1000)
     *        .withInfoLogger(myInfoLogger)
     *        .withErrorLogger(myErrorLogger)
     *        .build()
     *        .runOrThrow();
     * </pre>
     *
     * @param <X> Type of exception that can be thrown by the runnable object
     *
     * @see FlakyTestRunner
     * @see FlakyTestRunner.RunnableThrows
     */
    public interface Builder<X extends Throwable> {

        /**
         * Configures whether the runner is considered flaky on the current architecture.
         * <p>
         * Default value: true
         *
         * @param flakyOnThisArchitecture Flag indicating flakiness
         * @return This builder, allowing method chaining
         */
        Builder<X> withFlakyOnThisArchitecture(boolean flakyOnThisArchitecture);

        /**
         * Sets the maximum number of iterations before the runner fails.
         * <p>
         * This setting allows configuring how many times the runner will attempt the test before failing.
         * <p>
         * Default value: 2
         *
         * @param maxIterations non-negative value to use
         * @return this Builder, allowing method chaining
         */
        Builder<X> withMaxIterations(int maxIterations);

        /**
         * Sets the delay between each iteration in milliseconds.
         * <p>
         * This sets the time delay between retries if the test is failing.
         * <p>
         * Default value: 500 ms
         *
         * @param delayMs non-negative value to use
         * @return this Builder, allowing method chaining
         */
        Builder<X> withIterationDelay(long delayMs);

        /**
         * Sets if a garbage collection should be requested between each iteration.
         * <p>
         * Enabling this setting can help to clean up resources between test attempts.
         * <p>
         * Default value: true
         *
         * @param interIterationGc flag to enable or disable garbage collection between iterations
         * @return this Builder, allowing method chaining
         */
        Builder<X> withInterIterationGc(boolean interIterationGc);

        /**
         * Sets the info logger to use for informational messages.
         * <p>
         * Default value: System.out::println
         *
         * @param infoLogger Logger to use for informational messages
         * @return this Builder, allowing method chaining
         */
        Builder<X> withInfoLogger(Consumer<? super String> infoLogger);

        /**
         * Sets the error logger to use for error messages.
         * <p>
         * Default value: System.err::println
         *
         * @param errorLogger Logger to use for error messages
         * @return this Builder, allowing method chaining
         */
        Builder<X> withErrorLogger(Consumer<? super String> errorLogger);

        /**
         * Constructs a runnable object that encapsulates the configured flaky test behavior.
         * <p>
         * This method finalizes the builder and constructs a new runnable object to handle
         * the flaky test behavior based on the configured settings.
         * <p>
         * This method can only be called once per builder instance.
         *
         * @return A new RunnableThrows object that encapsulates the flaky test behavior
         */
        RunnableThrows<X> build();
    }
}
