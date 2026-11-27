/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.network.proxy;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyConnectionFailureTest {
    @Test
    void uncheckedConnectFailureStillCompletesCleanup() throws Exception {
        try (SocketChannel inbound = SocketChannel.open()) {
            ProxyConnection connection = new ProxyConnection(inbound,
                    InetSocketAddress.createUnresolved("unresolved.invalid", 12345));

            assertThrows(UnresolvedAddressException.class, connection::run);
            assertTrue(connection.isFinished(), "close must not wait for a task that has already failed");
            assertFalse(inbound.isOpen());
            connection.close();
        }
    }
}
