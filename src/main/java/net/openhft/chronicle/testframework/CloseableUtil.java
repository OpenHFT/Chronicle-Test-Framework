package net.openhft.chronicle.testframework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CloseableUtil {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableUtil.class);

    /**
     * Close a {@link java.io.Closeable}, logging any exceptions thrown
     *
     * @param closeable the closeable to close
     */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            LOGGER.warn("Error closing", e);
        }
    }
}
