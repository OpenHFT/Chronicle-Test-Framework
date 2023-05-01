package net.openhft.chronicle.testframework;

public enum ThreadUtil {
    ;

    /**
     * Pause interruptibly
     * <p>
     * Because we don't depend on chronicle-core, we don't have Jvm.pause(...)
     *
     * @param timeInMillis The amount of time to pause for in milliseconds
     */
    public static void pause(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
