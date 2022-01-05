package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

import static java.util.Objects.requireNonNull;

public final class VanillaFlakyTestRunnerBuilder<X extends Throwable> implements FlakyTestRunner.Builder<X> {

    final FlakyTestRunner.RunnableThrows<X> action;
    private boolean built = false;

    boolean flakyOnThisArchitecture = true;
    int maxIterations = 1;
    long delayMs = 500;
    boolean interIterationGc = true;
    PrintStream infoLogger = System.out;
    PrintStream errorLogger = System.err;

    public VanillaFlakyTestRunnerBuilder(@NotNull final FlakyTestRunner.RunnableThrows<X> action) {
        this.action = requireNonNull(action);
    }

    @Override
    public FlakyTestRunner.Builder<X> withFlakyOnThisArchitecture(boolean flakyOnThisArchitecture) {
        this.flakyOnThisArchitecture = flakyOnThisArchitecture;
        return this;
    }

    @Override
    public FlakyTestRunner.Builder<X> withMaxIterations(int maxIterations) {
        if (maxIterations < 0)
            throw new IllegalArgumentException("maxIterations is negative: " + maxIterations);
        this.maxIterations = maxIterations;
        return this;
    }

    @Override
    public FlakyTestRunner.Builder<X> withIterationDelay(long delayMs) {
        if (delayMs < 0)
            throw new IllegalArgumentException("delayMs is negative: " + delayMs);
        this.delayMs = delayMs;
        return this;
    }

    @Override
    public FlakyTestRunner.Builder<X> withInterIterationGc(boolean interIterationGc) {
        this.interIterationGc = interIterationGc;
        return this;
    }

    @Override
    public FlakyTestRunner.Builder<X> withInfoLogger(PrintStream infoLogger) {
        this.infoLogger = requireNonNull(infoLogger);
        return this;
    }

    @Override
    public FlakyTestRunner.Builder<X> withErrorLogger(PrintStream errorLogger) {
        this.errorLogger = requireNonNull(errorLogger);
        return this;
    }

    @Override
    public FlakyTestRunner.RunnableThrows<X> build() {
        if (built)
            throw new IllegalStateException("Builder already used");
        built = true;
        return new VanillaFlakyTestRunner<>(this);
    }
}
