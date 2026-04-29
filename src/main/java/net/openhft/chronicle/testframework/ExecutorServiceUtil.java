/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class providing helper methods to stop an {@link ExecutorService}
 * and wait for its termination. Two shutdown styles are offered: graceful via
 * {@link ExecutorService#shutdown()} and forcible via
 * {@link ExecutorService#shutdownNow()}.
 * <p>
 * In both cases the caller waits using
 * {@link ExecutorService#awaitTermination(long, TimeUnit)}. If the executor does
 * not terminate within the allotted time or the waiting thread is interrupted an
 * {@link IllegalStateException} is thrown. Interrupt status is restored before
 * the exception is propagated.
 * <p>
 * This enum has no instances and exists solely as a namespace.
 */
public enum ExecutorServiceUtil {
    ;

    private static final int DEFAULT_TIME_TO_WAIT_SECONDS = 5;

    /**
     * Initiates a graceful shutdown and waits for termination.
     * <p>
     * Calls {@link ExecutorService#shutdown()} so no new tasks are accepted. Any
     * tasks already running are left to finish. The calling thread then waits for
     * at most {@code timeout} in the supplied {@code unit}. If the executor still
     * has threads alive when the wait expires, or if the waiting thread is
     * interrupted, an {@link IllegalStateException} is thrown and the interrupt
     * status is preserved.
     *
     * @param executorService the executor to shut down
     * @param timeout         maximum time to wait
     * @param unit            unit of the timeout argument
     * @throws IllegalStateException if the executor did not terminate in time or
     *                               the waiting thread was interrupted
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdown();
        waitForTermination(executorService, timeout, unit);
    }

    /**
     * Variant of {@link #shutdownAndWaitForTermination(ExecutorService, long, TimeUnit)}
     * that waits for a default of five seconds.
     *
     * @param executorService the executor to shut down
     * @throws IllegalStateException if it does not terminate within five seconds
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService) {
        executorService.shutdown();
        waitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Forces shutdown by interrupting all threads and waits a default of five seconds
     * for the executor to finish.
     *
     * @param executorService the executor to stop
     * @throws IllegalStateException if it does not terminate within five seconds
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService) {
        shutdownForciblyAndWaitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Attempts to stop all active tasks and waits for termination.
     * <p>
     * Invokes {@link ExecutorService#shutdownNow()} to interrupt running tasks and
     * to discard queued tasks. The calling thread then waits for up to
     * {@code timeout} in the given {@code unit}. Should the executor fail to finish
     * within that window, or if the waiting thread is interrupted, an
     * {@link IllegalStateException} is thrown and the interrupt status is preserved.
     *
     * @param executorService the executor to stop
     * @param timeout         maximum time to wait
     * @param unit            unit of the timeout argument
     * @throws IllegalStateException if termination does not occur in time or if the wait is interrupted
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdownNow(); // Interrupt all running tasks
        waitForTermination(executorService, timeout, unit);
    }

    /**
     * Blocks until the executor terminates or the timeout elapses.
     * If the wait times out or the thread is interrupted an
     * {@link IllegalStateException} is thrown. Interrupt status is restored
     * before throwing.
     */
    private static void waitForTermination(ExecutorService executorService, long timeToWait, TimeUnit units) {
        try {
            if (!executorService.awaitTermination(timeToWait, units)) {
                throw new IllegalStateException("ExecutorService didn't shut down");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new IllegalStateException("Interrupted waiting for executor service to shut down", e);
        }
    }
}
