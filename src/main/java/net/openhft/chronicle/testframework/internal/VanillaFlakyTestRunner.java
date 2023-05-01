package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;

public final class VanillaFlakyTestRunner<X extends Throwable> implements FlakyTestRunner.RunnableThrows<X> {

    private final AtomicBoolean inRun = new AtomicBoolean();
    private final VanillaFlakyTestRunnerBuilder<X> builder;

    public VanillaFlakyTestRunner(final VanillaFlakyTestRunnerBuilder<X> builder) {
        this.builder = requireNonNull(builder);
    }

    @Override
    public void run() throws X {
        if (!inRun.compareAndSet(false, true))
            throw new AssertionError("Can't run nested");
        try {
            for (int i = 0; i < builder.maxIterations; i++) {
                try {
                    builder.action.run();
                    if (i > 0) {
                        builder.infoLogger.accept("Flaky test threw an error " + i + " run(s), but passed on run " + (i + 1));
                    }
                    break;
                } catch (Throwable x) {
                    // Using Throwable above allows AssertionError to be retried
                    if (i == (builder.maxIterations - 1)) {
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