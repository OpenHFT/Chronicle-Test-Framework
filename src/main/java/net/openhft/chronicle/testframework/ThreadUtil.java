package net.openhft.chronicle.testframework;

/**
 * Utility class for thread-related functionalities.
 * <p>
 * This enum class is used as a utility and does not have any instances.
 */
public enum ThreadUtil {
    ;

    /**
     * Pauses the current thread's execution for the specified amount of time.
     * If the thread is interrupted during the sleep, it will catch the
     * InterruptedException and set the interrupt status of the current thread.
     * <p>
     * This method is intended to provide similar functionality to Jvm.pause(...)
     * but does not depend on chronicle-core.
     *
     * @param timeInMillis The amount of time to pause for in milliseconds.
     */
    public static void pause(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
