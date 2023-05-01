package net.openhft.chronicle.testframework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public enum ExecutorServiceUtil {
    ;

    private static final int DEFAULT_TIME_TO_WAIT_SECONDS = 5;

    /**
     * Shut down an executor service, waiting for it to terminate
     *
     * @param executorService The executor service
     * @param timeout         The timeout
     * @param unit            The unit of the timeout argument
     * @throws IllegalStateException if it didn't terminate
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdown();
        waitForTermination(executorService, timeout, unit);
    }

    /**
     * Shut down an executor service, wait 5 seconds for it to terminate
     *
     * @param executorService The executor service
     * @throws IllegalStateException if it didn't terminate in time
     */
    public static void shutdownAndWaitForTermination(ExecutorService executorService) {
        executorService.shutdown();
        waitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Shut down an executor service, interrupting all threads, wait 5 seconds for it to terminate
     *
     * @param executorService The executor service
     * @throws IllegalStateException if it didn't terminate in time
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService) {
        shutdownForciblyAndWaitForTermination(executorService, DEFAULT_TIME_TO_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Shut down an executor service, interrupting all threads, wait for it to terminate
     *
     * @param executorService The executor service
     * @param timeout         The timeout
     * @param unit            The unit of the timeout argument
     * @throws IllegalStateException if it didn't terminate in time
     */
    public static void shutdownForciblyAndWaitForTermination(ExecutorService executorService, long timeout, TimeUnit unit) {
        executorService.shutdownNow();
        waitForTermination(executorService, timeout, unit);
    }

    private static void waitForTermination(ExecutorService executorService, long timeToWait, TimeUnit units) {
        try {
            if (!executorService.awaitTermination(timeToWait, units)) {
                throw new IllegalStateException("ExecutorService didn't shut down");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for executor service to shut down");
        }
    }
}
