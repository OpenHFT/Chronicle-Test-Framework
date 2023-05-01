package net.openhft.chronicle.testframework.internal.network.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import static net.openhft.chronicle.testframework.ThreadUtil.pause;

/**
 * A TCP Proxy, will proxy a single port to a single upstream port
 */
public class TcpProxy implements Closeable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxy.class);

    private final InetSocketAddress socketAddress;
    private final InetSocketAddress connectAddress;
    private final ExecutorService executorService;
    private final List<ProxyConnection> connections;
    private volatile boolean running;
    private volatile boolean finished = false;
    private volatile boolean acceptingNewConnections = true;
    private volatile boolean isOpen = false;

    public TcpProxy(int acceptPort, InetSocketAddress connectAddress, ExecutorService executorService) {
        this.connectAddress = connectAddress;
        this.executorService = executorService;
        this.connections = new CopyOnWriteArrayList<>();
        this.socketAddress = new InetSocketAddress(acceptPort);
    }

    public InetSocketAddress socketAddress() {
        return socketAddress;
    }

    @Override
    public void run() {
        running = true;
        LOGGER.info("Starting proxy on {} proxying to {}", socketAddress, connectAddress);
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
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
