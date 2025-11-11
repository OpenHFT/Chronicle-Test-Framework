/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
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
     * The method opens a {@link ServerSocket} on port {@code 0}, causing the
     * operating system to allocate an ephemeral port. The socket is closed
     * straight away, freeing the port. Another process may claim the same port
     * before the caller can bind to it, so this is best suited to test code.
     * Firewalls or security managers might also prevent the allocation.
     *
     * @return a port number that is likely to be available
     * @throws RuntimeException if an {@link IOException} occurs while creating
     *                          or closing the socket
     */
    public static int getAvailablePort() {
        // Binds a temporary socket to obtain an ephemeral port.
        // The port is released when the socket closes.
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find an available port", e);
        }
    }
}
