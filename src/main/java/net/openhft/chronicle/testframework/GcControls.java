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
 * Some utilities to try and reliably execute GCs.
 * <p>
 * Note this is inherently unreliable for a number of reasons so shouldn't
 * be expected to work.
 */
@SuppressWarnings("java:S1215") // Sonar rightfully tells us not to use System.gc
public enum GcControls {
    ;

    /**
     * Request a GC
     */
    public static void requestGcCycle() {
        System.gc();
    }

    /**
     * Request a GC and block until it occurs
     *
     * @throws IllegalStateException if the GC doesn't occur within one second
     */
    public static void waitForGcCycle() {
        final long gcCount = getGcCount();
        System.gc();
        final long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1L);
        while ((getGcCount() == gcCount) && System.currentTimeMillis() < timeoutAt) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10L));
        }

        if (getGcCount() == gcCount) {
            throw new IllegalStateException("GC did not occur within timeout");
        }
    }

    /**
     * Get the GC count
     *
     * @return The number of GCs of all types that have occurred
     */
    public static long getGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream().
                reduce(0L,
                        (count, gcBean) -> count + gcBean.getCollectionCount(),
                        Long::sum);
    }
}
