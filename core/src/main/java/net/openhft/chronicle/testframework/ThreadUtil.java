/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

/**
 * Utility class for thread-related functionalities.
 * <p>
 * This enum class is used as a utility and does not have any instances.
 */
public enum ThreadUtil {
    ;

    /**
     * Pause the current thread for the supplied time in milliseconds.
     * <p>
     * The actual delay can be slightly longer than requested, typically around
     * one millisecond due to scheduler granularity. If the thread is interrupted
     * while sleeping, the interrupt status is restored and the method returns
     * without throwing.
     * <p>
     * This mirrors the behaviour of {@code Jvm.pause(...)} but does not depend
     * on chronicle-core.
     *
     * @param timeInMillis the time to pause in milliseconds
     */
    public static void pause(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
