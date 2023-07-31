package net.openhft.chronicle.testframework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class providing methods to shut down an {@link ExecutorService}
 * and wait for its termination, with or without forcibly interrupting running tasks.
 * <p>
 * This is an enum with no instances, serving solely for namespace control.
 */
public enum ExecutorServiceUtil {
    ;

    private static final int DEFAULT_TIME_TO_WAIT_SECONDS = 5;

    /**
     * Shut down an executor service and wait for it to terminate within the given timeout.
     *
     * @param executorService The executor service
     * @param timeout         The maximum time to wait
     * @param unit            The unit of the timeout argument
     * @throws IllegalStateException If the executor service didn't terminate within the timeout
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdown();
        waitForTermination(executorService, timeout, unit);
    }

    /**
     * Shut down an executor service and wait 5 seconds for it to terminate.
     *
     * @param executorService The executor service
     * @throws IllegalStateException If it didn't terminate within the default time of 5 seconds
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService) {
        executorService.shutdown();
        waitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Shut down an executor service, interrupting all threads, and wait 5 seconds for it to terminate.
     *
     * @param executorService The executor service
     * @throws IllegalStateException If it didn't terminate within the default time of 5 seconds
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService) {
        shutdownForciblyAndWaitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Shut down an executor service, interrupting all threads, and wait for it to terminate within the given timeout.
     *
     * @param executorService The executor service
     * @param timeout         The maximum time to wait
     * @param unit            The unit of the timeout argument
     * @throws IllegalStateException If it didn't terminate within the specified timeout
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdownNow(); // Interrupt all running tasks
        waitForTermination(executorService, timeout, unit);
    }

    // Waits for the ExecutorService to terminate within the specified time
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
