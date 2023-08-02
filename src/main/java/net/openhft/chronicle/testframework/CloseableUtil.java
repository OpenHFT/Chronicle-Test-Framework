package net.openhft.chronicle.testframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle closing of {@link java.io.Closeable} or {@link AutoCloseable} resources.
 * This class provides a method to close a resource without throwing any exceptions, logging them instead.
 * Since this class is defined as an enum without instances, it cannot be instantiated and is essentially a utility class.
 */
public enum CloseableUtil {
    ; // This enum has no instances, acting as a utility class

    // Logger to log warning messages if any exceptions occur while closing the resource
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableUtil.class);

    /**
     * Closes the provided {@link AutoCloseable} resource quietly, without throwing any exceptions.
     * If an exception does occur while closing the resource, it is logged as a warning and not propagated.
     *
     * @param closeable the closeable resource to close, may be any object implementing {@link AutoCloseable}
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
