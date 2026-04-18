/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Utilities for interacting with the JVM's garbage collector.
 * <p>
 * These methods attempt to trigger a GC cycle and optionally wait for one to
 * occur. The JVM treats explicit requests as hints only, so a cycle may not run
 * immediately or at all. Waiting relies on the GC count increasing and does not
 * guarantee that any memory has been reclaimed.
 * <p>
 * Behaviour differs between JVM vendors and collector implementations. Some
 * collectors ignore {@code System.gc()} requests and the available collection
 * counters vary.
 */
@SuppressWarnings("java:S1215") // Sonar warning suppression for System.gc usage
public enum GcControls {
    ; // Enum with no instances signifies a utility class

    /**
     * Requests that the JVM performs a garbage collection cycle.
     * <p>
     * The call merely hints that a collection would be useful. The JVM may
     * ignore it or defer the cycle. No guarantee is made that a cycle runs at
     * once or that this method blocks until it completes.
     */
    public static void requestGcCycle() {
        System.gc(); // Request a garbage collection cycle
    }

    /**
     * Requests a collection and waits for the GC count to increase.
     * <p>
     * The method polls {@link #getGcCount()} every ten milliseconds for around
     * one second after calling {@link #requestGcCycle()}. If the count does not
     * change in that period an {@link IllegalStateException} is thrown. A
     * successful return only indicates that a GC cycle was observed, not that
     * memory has been reclaimed.
     *
     * @throws IllegalStateException if no GC cycle is detected within the time-out
     */
    public static void waitForGcCycle() {
        final long gcCount = getGcCount(); // Initial GC count
        System.gc(); // Request a garbage collection cycle
        final long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1L);
        while ((getGcCount() == gcCount) && System.currentTimeMillis() < timeoutAt) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10L)); // Wait for 10 ms
        }

        if (getGcCount() == gcCount) {
            throw new IllegalStateException("GC did not occur within timeout"); // Throw an exception if GC did not occur
        }
    }

    /**
     * Retrieves the total count of garbage collection (GC) cycles that have occurred.
     * <p>
     * The counts reported by every collector MXBean are summed so the result
     * reflects all collectors active in the JVM.
     *
     * @return the number of GCs of all types that have occurred
     */
    public static long getGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .reduce(0L,
                        (count, gcBean) -> count + gcBean.getCollectionCount(), // Sum the collection counts
                        Long::sum); // Combine the sums
    }
}
