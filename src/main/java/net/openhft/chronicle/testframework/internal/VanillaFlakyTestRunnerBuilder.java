package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Internal builder for {@link FlakyTestRunner} using the vanilla implementation.
 *
 * <p>It allows callers to control retry count, delays, optional garbage
 * collection and the loggers used.  A builder can only be built once.
 */
public final class VanillaFlakyTestRunnerBuilder<X extends Throwable> implements FlakyTestRunner.Builder<X> {

    final FlakyTestRunner.RunnableThrows<X> action;
    boolean flakyOnThisArchitecture = true;
    int maxIterations = 1;
    long delayMs = 500;
    boolean interIterationGc = true;
    Consumer<? super String> infoLogger = System.out::println;
    Consumer<? super String> errorLogger = System.err::println;
    private boolean built = false;

    /**
     * Creates a builder for the supplied action.
     */
    public VanillaFlakyTestRunnerBuilder(@NotNull final FlakyTestRunner.RunnableThrows<X> action) {
        this.action = requireNonNull(action);
    }

    /**
     * Marks whether retries are needed on the current architecture.
     */
    @Override
    public FlakyTestRunner.Builder<X> withFlakyOnThisArchitecture(boolean flakyOnThisArchitecture) {
        this.flakyOnThisArchitecture = flakyOnThisArchitecture;
        return this;
    }

    /**
     * Sets how many attempts will be made before the test fails.
     */
    @Override
    public FlakyTestRunner.Builder<X> withMaxIterations(int maxIterations) {
        if (maxIterations < 0)
            throw new IllegalArgumentException("maxIterations is negative: " + maxIterations);
        this.maxIterations = maxIterations;
        return this;
    }

    /**
     * Sets the pause in milliseconds between attempts.
     */
    @Override
    public FlakyTestRunner.Builder<X> withIterationDelay(long delayMs) {
        if (delayMs < 0)
            throw new IllegalArgumentException("delayMs is negative: " + delayMs);
        this.delayMs = delayMs;
        return this;
    }

    /**
     * Requests a garbage collection between retries when true.
     */
    @Override
    public FlakyTestRunner.Builder<X> withInterIterationGc(boolean interIterationGc) {
        this.interIterationGc = interIterationGc;
        return this;
    }

    /**
     * Sets the logger used for informational messages.
     */
    @Override
    public FlakyTestRunner.Builder<X> withInfoLogger(Consumer<? super String> infoLogger) {
        this.infoLogger = requireNonNull(infoLogger);
        return this;
    }

    /**
     * Sets the logger used when an attempt fails.
     */
    @Override
    public FlakyTestRunner.Builder<X> withErrorLogger(Consumer<? super String> errorLogger) {
        this.errorLogger = requireNonNull(errorLogger);
        return this;
    }

    /**
     * Finalises the builder and returns the runnable test.
     */
    @Override
    public FlakyTestRunner.RunnableThrows<X> build() {
        if (built)
            throw new IllegalStateException("Builder already used");
        built = true;
        return new VanillaFlakyTestRunner<>(this);
    }
}
