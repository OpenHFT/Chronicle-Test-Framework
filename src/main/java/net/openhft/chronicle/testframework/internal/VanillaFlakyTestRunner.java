package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;

/**
 * Executes a flaky test action with retry semantics.
 *
 * <p>The runner is thread-safe in that only one {@link #run()} call may
 * execute at a time. Concurrent or nested invocations result in an
 * {@link AssertionError}. The action is retried until it succeeds or the
 * configured number of attempts is exhausted.
 */
public final class VanillaFlakyTestRunner<X extends Throwable>
        implements FlakyTestRunner.RunnableThrows<X> {

    private final AtomicBoolean inRun = new AtomicBoolean();
    private final VanillaFlakyTestRunnerBuilder<X> builder;

    public VanillaFlakyTestRunner(final VanillaFlakyTestRunnerBuilder<X> builder) {
        this.builder = requireNonNull(builder);
    }

    /**
     * Executes the action until it succeeds or the retry limit is reached.
     *
     * <p>If the action fails on an intermediate attempt the error logger is
     * invoked, optional garbage collection is requested and the thread pauses
     * for the configured delay. The method rethrows the final exception if all
     * attempts fail.
     *
     * @throws X if the action fails on the last try
     */
    @Override
    public void run() throws X {
        if (!inRun.compareAndSet(false, true))
            throw new AssertionError("Can't run nested");
        try {
            final int maxIterations = builder.flakyOnThisArchitecture ? builder.maxIterations : Math.max(1, builder.maxIterations);
            for (int i = 0; i < maxIterations; i++) {
                try {
                    builder.action.run();
                    if (i > 0) {
                        builder.infoLogger.accept("Flaky test threw an error " + i + " run(s), but passed on run " + (i + 1));
                    }
                    break;
                } catch (Throwable x) {
                    // Using Throwable above allows AssertionError to be retried
                    if (i == (maxIterations - 1)) {
                        throw x;
                    }
                    builder.errorLogger.accept("Rerunning failing test run " + (i + 2));
                    if (builder.interIterationGc) {
                        System.gc();
                    }
                    pause(builder.delayMs);
                }
            }
        } finally {
            inRun.set(false);
        }
    }
}
