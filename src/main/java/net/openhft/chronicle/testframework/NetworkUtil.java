package net.openhft.chronicle.testframework;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility class to provide network-related functionalities.
 * <p>
 * This class includes methods to interact with network resources, such as finding an available port.
 */
public enum NetworkUtil {
    ; // Enum with no instances signifies a utility class

    /**
     * Retrieves an available port number on the local machine.
     * <p>
     * This method attempts to bind to an automatically allocated port, then returns the port number.
     * It provides a simple way to find a port that's very likely to be available.
     *
     * @return a port number that is likely to be available for binding
     * @throws RuntimeException if an {@link IOException} occurs while trying to create a {@link ServerSocket}
     */
    public static int getAvailablePort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) { // Attempt to bind to an automatically allocated port
            return serverSocket.getLocalPort(); // Return the port number
        } catch (IOException e) {
            throw new RuntimeException("Failed to find an available port", e); // Propagate the exception as a runtime exception
        }
    }
}
