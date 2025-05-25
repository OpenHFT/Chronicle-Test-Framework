/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 */
@SuppressWarnings("java:S1215") // Sonar warning suppression for System.gc usage
public enum GcControls {
    ; // Enum with no instances signifies a utility class

    /**
     * Requests that the JVM performs a garbage collection cycle.
     * <p>
     * The call merely hints that a collection would be useful. The JVM may
     * ignore it or defer the cycle. No guarantee is made that a GC has run when
     * this method returns.
     */
    public static void requestGcCycle() {
        System.gc(); // Request a garbage collection cycle
    }

    /**
     * Requests a collection and waits for the GC count to increase.
     * <p>
     * The method polls the collector count for up to one second after calling
     * {@link #requestGcCycle()}. If the count does not change in that period an
     * {@link IllegalStateException} is thrown. A successful return only indicates
     * that a GC cycle was observed, not that memory has been reclaimed.
     *
     * @throws IllegalStateException if no GC cycle is detected within the timeout
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
     * This method returns the sum of collection counts for all types of garbage collectors
     * within the JVM.
     *
     * @return The number of GCs of all types that have occurred
     */
    public static long getGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .reduce(0L,
                        (count, gcBean) -> count + gcBean.getCollectionCount(), // Sum the collection counts
                        Long::sum); // Combine the sums
    }
}
