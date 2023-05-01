package net.openhft.chronicle.testframework;

import java.io.IOException;
import java.net.ServerSocket;

public enum NetworkUtil {
    ;

    /**
     * Get a port number that's very likely to be available
     *
     * @return a port number that can be opened
     */
    public static int getAvailablePort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
