/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.testframework.internal.network.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.openhft.chronicle.testframework.CloseableUtil.closeQuietly;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;

/**
 * A lightweight TCP proxy that listens on one port and forwards all traffic to
 * a single upstream port. When created with an ephemeral accept port (0), the
 * {@link #socketAddress()} method waits for binding to complete so that
 * the chosen port can be reported.
 */
public class TcpProxy implements Closeable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxy.class);
    private static final long SERVER_SOCKET_OPEN_WAIT_TIME = 10_000;

    private final InetSocketAddress socketAddress;
    private final InetSocketAddress connectAddress;
    private final ExecutorService executorService;
    private final List<ProxyConnection> connections;
    private volatile boolean running;
    private volatile boolean finished = false;
    private volatile boolean acceptingNewConnections = true;
    private volatile boolean isOpen = false;
    private ServerSocketChannel serverSocket;
    private final CompletableFuture<InetSocketAddress> boundAddress = new CompletableFuture<>();

    /**
     * Creates a proxy that listens on the supplied port and forwards to the
     * given address. Passing {@code 0} uses an ephemeral port.
     */
    public TcpProxy(int acceptPort, InetSocketAddress connectAddress, ExecutorService executorService) {
        this.connectAddress = connectAddress;
        this.executorService = executorService;
        this.connections = new CopyOnWriteArrayList<>();
        this.socketAddress = new InetSocketAddress(acceptPort);
    }

    /**
     * Creates a proxy with an ephemeral accept port.
     */
    public TcpProxy(InetSocketAddress connectAddress, ExecutorService executorService) {
        this(0, connectAddress, executorService);
    }

    /**
     * Waits up to ten seconds for binding and non-blocking configuration to finish.
     * Startup failure retains its cause; interruption is preserved.
     */
    public InetSocketAddress socketAddress() {
        try {
            return boundAddress.get(SERVER_SOCKET_OPEN_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for proxy bind", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Proxy could not bind " + socketAddress, e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Timed out waiting for proxy to bind " + socketAddress, e);
        }
    }

    @Override
    public void run() {
        running = true;
        LOGGER.info("Starting proxy on {} proxying to {}", socketAddress, connectAddress);
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(socketAddress, 10);
            serverSocket.configureBlocking(false);
            boundAddress.complete((InetSocketAddress) serverSocket.getLocalAddress());
            while (running) {
                isOpen = true;
                if (acceptingNewConnections)
                    acceptConnection();
                connections.removeIf(ProxyConnection::isFinished);
                if (!serverSocket.isOpen()) {
                    throw new IllegalStateException("Server socket not open");
                }
                pause(10);
            }
        } catch (Exception e) {
            boundAddress.completeExceptionally(e);
            LOGGER.error("proxy run failed", e);
        } finally {
            closeQuietly(serverSocket);
            isOpen = false;
        }
        LOGGER.info("TCP proxy from {} proxying to {} terminated", socketAddress, connectAddress);
        finished = true;
    }

    private void acceptConnection() throws IOException {
        final SocketChannel newConnection = serverSocket.accept();
        if (newConnection == null)
            return;
        LOGGER.info("Received inbound connection from {}", newConnection.socket().getRemoteSocketAddress());
        final ProxyConnection connection = new ProxyConnection(newConnection, connectAddress);
        connections.add(connection);
        try {
            executorService.submit(connection);
        } catch (RuntimeException | Error failure) {
            connections.remove(connection);
            // A rejected task never runs the connection's channel cleanup or finishes its close wait.
            closeQuietly(newConnection);
            throw failure;
        }
    }

    /**
     * Closes all active connections and stops accepting new ones until
     * {@link #acceptNewConnections()} is called.
     */
    public void dropConnectionsAndPauseNewConnections() {
        acceptingNewConnections = false;
        connections.forEach(ProxyConnection::close);
    }

    /**
     * Keeps current connections open but stops forwarding traffic. New
     * connections are rejected until {@link #acceptNewConnections()} is
     * invoked.
     */
    public void stopForwardingTrafficAndPauseNewConnections() {
        acceptingNewConnections = false;
        connections.forEach(ProxyConnection::stopForwardingTraffic);
    }

    /**
     * Allows new connections after a pause.
     */
    public void acceptNewConnections() {
        acceptingNewConnections = true;
    }

    /**
     * Stops the proxy and waits for all resources to close.
     */
    @Override
    public void close() throws IllegalStateException {
        running = false;
        acceptingNewConnections = false;
        connections.forEach(ProxyConnection::close);
        while (!finished) {
            pause(10);
        }
    }

    /**
     * Reports whether the server socket is bound and ready for connections.
     *
     * @return true if open
     */
    public boolean isOpen() {
        return isOpen;
    }
}
