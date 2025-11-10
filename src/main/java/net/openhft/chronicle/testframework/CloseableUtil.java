//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.testframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for closing {@link java.io.Closeable} or {@link AutoCloseable} resources.
 * The enum defines no instances and therefore holds no state. A static
 * {@link org.slf4j.Logger} records any exception at WARN level. The absence of
 * instance state means the class is thread-safe.
 */
public enum CloseableUtil {
    ; // This enum has no instances, acting as a utility class

    // Logger to log warning messages if any exceptions occur while closing the resource
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableUtil.class);

    /**
     * Closes the provided {@link AutoCloseable} quietly without propagating exceptions.
     * Intended for use in {@code finally} blocks where further failures must be suppressed.
     * Any exception is logged at WARN level using the static logger. The method is
     * thread-safe because the enum holds no instance state.
     *
     * @param closeable the resource to close, may be {@code null}
     */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) { // Ensuring the closeable is not null to avoid NullPointerException
                closeable.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Error closing", e); // Logging any exceptions that occur during closing
        }
    }
}
