/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.network.proxy;

import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.lang.reflect.Field;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class TcpProxyReadinessTest {
    @Test
    void openUnboundChannelIsNotPublished() throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
        TcpProxy proxy = new TcpProxy(new InetSocketAddress("localhost", 1), executor);
        try (ServerSocketChannel unbound = ServerSocketChannel.open()) {
            Field field = TcpProxy.class.getDeclaredField("serverSocket");
            field.setAccessible(true);
            field.set(proxy, unbound);
            CountDownLatch started = new CountDownLatch(1);
            Future<InetSocketAddress> address = executor.submit(() -> {
                started.countDown();
                return proxy.socketAddress();
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> address.get(100, TimeUnit.MILLISECONDS));
            executor.submit(proxy);
            try {
                assertTrue(address.get(5, TimeUnit.SECONDS).getPort() > 0);
            } finally {
                proxy.close();
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void bindFailureReachesAddressCaller() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ServerSocketChannel occupied = ServerSocketChannel.open()) {
            // Match the proxy's wildcard bind, including its address family on macOS.
            occupied.bind(new InetSocketAddress(0));
            TcpProxy proxy = new TcpProxy(((InetSocketAddress) occupied.getLocalAddress()).getPort(),
                    new InetSocketAddress("localhost", 1), executor);
            Future<?> run = executor.submit(proxy);
            try {
                IllegalStateException failure = assertThrows(IllegalStateException.class, proxy::socketAddress);
                assertInstanceOf(java.net.BindException.class, failure.getCause());
                run.get(5, TimeUnit.SECONDS);
            } finally {
                proxy.close();
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void interruptionIsPreserved() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            TcpProxy proxy = new TcpProxy(new InetSocketAddress("localhost", 1), executor);
            Thread.currentThread().interrupt();
            IllegalStateException failure = assertThrows(IllegalStateException.class, proxy::socketAddress);
            assertInstanceOf(InterruptedException.class, failure.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
            executor.shutdownNow();
        }
    }
}
