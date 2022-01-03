package net.openhft.chronicle.testframework.internal;

import net.openhft.chronicle.testframework.FlakyTestRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public final class VanillaFlakyTestRunner {

    private final AtomicBoolean inRun = new AtomicBoolean();

    public <X extends Throwable> void run(final FlakyTestRunner.RunnableThrows<X> action,
                                          final int maxIterations) throws X {
        requireNonNull(action);
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations is non positive " + maxIterations);
        }
        if (!inRun.compareAndSet(false, true))
            throw new AssertionError("Can't run nested");
        try {
            for (int i = 0; i < maxIterations; i++) {
                try {
                    action.run();
                    if (i > 0) {
                        System.out.println("Flaky test threw an error " + i + " run(s), but passed on run " + (i + 1));
                    }
                    break;
                } catch (Throwable t) {
                    if (i == (maxIterations - 1)) {
                        throw t;
                    }
                    System.err.println("Rerunning failing test run " + (i + 2));
                    System.gc();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignore) {
                        // do nothing
                    }
                }
            }
        } finally {
            inRun.set(false);
        }
    }
}