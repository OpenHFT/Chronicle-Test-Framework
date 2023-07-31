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
 * Provides utility methods for interacting with garbage collection (GC) within the JVM.
 * <p>
 * These utilities are used to request and wait for garbage collection cycles.
 * Please note that relying on explicit garbage collection is inherently unreliable,
 * and these methods should be used with caution.
 */
@SuppressWarnings("java:S1215") // Sonar warning suppression for System.gc usage
public enum GcControls {
    ; // Enum with no instances signifies a utility class

    /**
     * Requests a garbage collection (GC) cycle to be performed.
     * <p>
     * This method is a hint to the JVM, and there's no guarantee the GC will actually be performed.
     */
    public static void requestGcCycle() {
        System.gc(); // Request a garbage collection cycle
    }

    /**
     * Requests a garbage collection (GC) cycle and blocks until it occurs or a timeout is reached.
     * <p>
     * This method waits for up to one second for a GC cycle to occur. If the GC does not occur
     * within this time, an {@code IllegalStateException} is thrown.
     *
     * @throws IllegalStateException if the GC doesn't occur within one second
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
