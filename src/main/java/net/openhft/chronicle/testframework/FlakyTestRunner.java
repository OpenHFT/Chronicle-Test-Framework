package net.openhft.chronicle.testframework;

import net.openhft.chronicle.testframework.internal.VanillaFlakyTestRunnerBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class FlakyTestRunner {

    // Suppresses default constructor, ensuring non-instantiability.
    private FlakyTestRunner() {
    }

    /**
     * Runs the provided {@code action} trying at most 2 times.
     *
     * @param action non-null action to perform
     * @param <X>    exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    @Deprecated // for removal in x.22. Use the builder instead
    public static <X extends Throwable> void run(@NotNull final RunnableThrows<X> action) throws X {
        builder(action)
                .build()
                .run();
    }

    /**
     * Runs the provided {@code action} trying at most 2 times if the provided {@code flakyOnTHisArch} is true,
     * otherwise just runs the provided {@code action} once.
     *
     * @param flakyOnThisArch indicating if the provided action is
     * @param action          non-null action to perform
     * @param <X>             exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    @Deprecated // for removal in x.22. Use the builder instead
    public static <X extends Throwable> void run(final boolean flakyOnThisArch,
                                                 @NotNull final RunnableThrows<X> action) throws X {
        builder(action)
                .withFlakyOnThisArchitecture(flakyOnThisArch)
                .build()
                .run();
    }

    /**
     * Runs the provided {@code action} trying at most the provided {@code maxIteration} times if the
     * provided {@code flakyOnTHisArch} is true, otherwise just runs the provided {@code action} once.
     *
     * @param flakyOnThisArch indicating if the provided action is
     * @param action          non-null action to perform
     * @param <X>             exception type
     * @throws X if an underlying exception is thrown despite retrying the specified number of times
     */
    @Deprecated // for removal in x.22. Use the builder instead
    public static <X extends Throwable> void run(final boolean flakyOnThisArch,
                                                 @NotNull final RunnableThrows<X> action,
                                                 final int maxIterations) throws X {
        builder(action)
                .withFlakyOnThisArchitecture(flakyOnThisArch)
                .withMaxIterations(maxIterations)
                .build()
                .run();
    }

    @FunctionalInterface
    public interface RunnableThrows<T extends Throwable> {

        /**
         * Performs an action.
         *
         * @throws T if an underlying exception is thrown
         */
        void run() throws T;
    }

    public static <X extends Throwable> Builder<X> builder(@NotNull final RunnableThrows<X> action) {
        requireNonNull(action);
        return new VanillaFlakyTestRunnerBuilder<>(action);
    }

    public interface Builder<X extends Throwable> {

        /**
         * Sets if the runner is flaky on this architecture.
         * <p>
         * Default value: true
         *
         * @param flakyOnThisArchitecture to use
         * @return this Builder
         */
        Builder<X> withFlakyOnThisArchitecture(boolean flakyOnThisArchitecture);

        /**
         * Sets the maximum number of iteration before the runner fails.
         * <p>
         * Default value: 2
         *
         * @param maxIterations non-negative value to use
         * @return this Builder
         */
        Builder<X> withMaxIterations(int maxIterations);

        /**
         * Sets the delay between each iteration in ms.
         * <p>
         * Default value: 500 ms
         *
         * @param delayMs non-negative value to use
         * @return this Builder
         */
        Builder<X> withIterationDelay(long delayMs);

        /**
         * Sets if a garbage collect should be requested between each iterations.
         * <p>
         * Default value: true
         *
         * @param interIterationGc to use
         * @return this Builder
         */
        Builder<X> withInterIterationGc(boolean interIterationGc);

        /**
         * Sets the info logger to use for messages.
         * <p>
         * Default value: System.out::println
         *
         * @param infoLogger to use
         * @return this Builder
         */
        Builder<X> withInfoLogger(Consumer<? super String> infoLogger);

        /**
         * Sets the error logger to use for messages.
         * <p>
         * Default value: System.err::println
         *
         * @param errorLogger to use
         * @return this Builder
         */
        Builder<X> withErrorLogger(Consumer<? super String> errorLogger);

        /**
         * Creates a Runnable that runs the provided runnable according to the
         * Builder's parameters.
         * <p>
         * This method can only be invoked once.
         *
         * @return a new RunnableThrows
         */
        RunnableThrows<X> build();
    }
}