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

import static net.openhft.chronicle.testframework.CloseableUtil.closeQuietly;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;
import static net.openhft.chronicle.testframework.Waiters.waitForCondition;

/**
 * A TCP Proxy, will proxy a single port to a single upstream port
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
     * Create a tcp proxy with the specified accept port.
     */
    public TcpProxy(int acceptPort, InetSocketAddress connectAddress, ExecutorService executorService) {
        this.connectAddress = connectAddress;
        this.executorService = executorService;
        this.connections = new CopyOnWriteArrayList<>();
        this.socketAddress = new InetSocketAddress(acceptPort);
    }

    /**
     * Create a tcp proxy with an ephemeral accept port.
     */
    public TcpProxy(InetSocketAddress connectAddress, ExecutorService executorService) {
        this(0, connectAddress, executorService);
    }

    /**
     * @return The socket address used for accepting connections. If an ephemeral port has been used (0) then this
     * method will wait up to {@link TcpProxy#SERVER_SOCKET_OPEN_WAIT_TIME} milliseconds for the server socket to be
     * non-null and open so that the address and port can be queried.
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

    @Override
    public void run() {
        running = true;
        LOGGER.info("Starting proxy on {} proxying to {}", socketAddress, connectAddress);
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(socketAddress, 10);
            serverSocket.configureBlocking(false);
            while (running) {
                isOpen = true;
                if (acceptingNewConnections) {
                    final SocketChannel newConnection = serverSocket.accept();
                    if (newConnection != null) {
                        LOGGER.info("Received inbound connection from {}", newConnection.socket().getRemoteSocketAddress());
                        final ProxyConnection connection = new ProxyConnection(newConnection, connectAddress);
                        connections.add(connection);
                        executorService.submit(connection);
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
        } catch (Exception e) {
            LOGGER.error("proxy run failed", e);
        } finally {
            closeQuietly(serverSocket);
            isOpen = false;
        }
        LOGGER.info("TCP proxy from {} proxying to {} terminated", socketAddress, connectAddress);
        finished = true;
    }

    public void dropConnectionsAndPauseNewConnections() {
        acceptingNewConnections = false;
        connections.forEach(ProxyConnection::close);
    }

    public void stopForwardingTrafficAndPauseNewConnections() {
        acceptingNewConnections = false;
        connections.forEach(ProxyConnection::stopForwardingTraffic);
    }

    public void acceptNewConnections() {
        acceptingNewConnections = true;
    }

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
     * Is the proxy open for connection
     *
     * @return true if the socket is open and ready to accept connections, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }
}
