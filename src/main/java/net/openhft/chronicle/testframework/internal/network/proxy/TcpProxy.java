package net.openhft.chronicle.testframework.internal.network.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import static net.openhft.chronicle.testframework.CloseableUtil.closeQuietly;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;
import static net.openhft.chronicle.testframework.Waiters.waitForCondition;

/**
 * A lightweight TCP proxy that listens on one port and forwards all traffic to
 * a single upstream port. When created with an ephemeral accept port (0), the
 * {@link #socketAddress()} method waits for the server socket to open so that
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
     * Returns the address on which the proxy accepts connections. When the
     * proxy is constructed with port {@code 0} this method waits up to
     * {@link #SERVER_SOCKET_OPEN_WAIT_TIME} ms for the server socket to open so
     * that the allocated port can be obtained.
     */
    public InetSocketAddress socketAddress() {
        if (socketAddress.getPort() == 0) {
            LOGGER.info("TcpProxy was instantiated with an ephemeral accept port. Waiting for up to {} milliseconds for " +
                    "server socket to be established so that chosen port can be determined.", SERVER_SOCKET_OPEN_WAIT_TIME);
            waitForCondition("TcpProxy configured to accept on an ephemeral port and timed out waiting to retrieve the socket address",
                    () -> serverSocket != null && serverSocket.isOpen(), SERVER_SOCKET_OPEN_WAIT_TIME);
            try {
                return (InetSocketAddress) serverSocket.getLocalAddress();
            } catch (IOException e) {
                throw new IllegalStateException("Could not retrieve local address", e);
            }
        } else {
            return socketAddress;
        }
    }

    /**
     * Accepts inbound connections and relays traffic to the upstream address.
     * Intended to be run on its own thread.
     */
    @Override
    public void run() {
        running = true;
        LOGGER.info("Starting proxy on {} proxying to {}", sanitize(socketAddress), sanitize(connectAddress));
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(socketAddress, 10);
            serverSocket.configureBlocking(false);
            while (running) {
                isOpen = true;
                if (acceptingNewConnections) {
                    final SocketChannel newConnection = serverSocket.accept();
                    if (newConnection != null) {
                        LOGGER.info("Received inbound connection from {}", sanitize(newConnection.socket().getRemoteSocketAddress()));
                        final ProxyConnection connection = new ProxyConnection(newConnection, connectAddress);
                        connections.add(connection);
                        executorService.execute(connection);
                    }
                }
                for (int i = 0; i < connections.size(); i++) {
                    if (connections.get(i).isFinished()) {
                        connections.remove(i);
                        i--;
                    }
                }
                if (!serverSocket.isOpen()) {
                    throw new IllegalStateException("Server socket not open");
                }
                pause(10);
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("proxy run failed", e);
        } finally {
            closeQuietly(serverSocket);
            isOpen = false;
        }
        LOGGER.info("TCP proxy from {} proxying to {} terminated", sanitize(socketAddress), sanitize(connectAddress));
        finished = true;
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

    private static String sanitize(SocketAddress address) {
        if (address == null) {
            return null;
        }
        return address.toString().replace('\r', ' ').replace('\n', ' ');
    }
}
