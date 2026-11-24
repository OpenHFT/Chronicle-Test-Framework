/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.network.proxy;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class TcpProxyRejectedConnectionTest {
    @Test
    void rejectedConnectionIsClosedAndDoesNotPreventProxyClosure() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.shutdown();
        TcpProxy proxy = new TcpProxy(new InetSocketAddress("127.0.0.1", 1), executor);
        Thread thread = new Thread(proxy, "rejected-connection-proxy");
        thread.setDaemon(true);
        thread.start();
        try {
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (!proxy.isOpen() && thread.isAlive() && System.nanoTime() < deadline)
                net.openhft.chronicle.testframework.ThreadUtil.pause(1);
            assertTrue(proxy.isOpen());
            try (Socket client = new Socket()) {
                client.connect(new InetSocketAddress("127.0.0.1", proxy.socketAddress().getPort()), 3000);
                client.setSoTimeout(3000);
                thread.join(5000);
                assertFalse(thread.isAlive(), "Proxy should finish after submission is rejected");
                assertEquals(-1, client.getInputStream().read(), "Rejected socket should reach EOF");
            }
            assertTrue(connections(proxy).isEmpty(), "Rejected task must not remain in the close wait list");
            proxy.close();
        } finally {
            // Also release a leaked accepted socket when this regression runs against the old code.
            List<?> connections = connections(proxy);
            Field inbound = ProxyConnection.class.getDeclaredField("inboundChannel");
            inbound.setAccessible(true);
            for (Object connection : connections)
                ((SocketChannel) inbound.get(connection)).close();
            connections.clear();
            if (!thread.isAlive())
                proxy.close();
            executor.shutdownNow();
        }
    }

    private static List<?> connections(TcpProxy proxy) throws Exception {
        Field field = TcpProxy.class.getDeclaredField("connections");
        field.setAccessible(true);
        return (List<?>) field.get(proxy);
    }
}
