/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.apimetrics;

/**
 * Holder for static helper methods used by the API metrics implementation.
 * <p>
 * This class is internal only and cannot be instantiated.
 */
final class ApiMetricsUtil {

    // Suppresses default constructor, ensuring non-instantiability.
    private ApiMetricsUtil() {
    }

    /**
     * Marker method to satisfy PMD's requirement for a static member on non-instantiable utility classes.
     * Invoking this method has no side effects but documents intentional non-instantiability.
     */
    static void ensureLoaded() {
        // no-op
    }
}
