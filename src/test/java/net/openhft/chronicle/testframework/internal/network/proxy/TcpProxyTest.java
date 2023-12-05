package net.openhft.chronicle.testframework.internal.network.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.openhft.chronicle.testframework.ExecutorServiceUtil.shutdownAndWaitForTermination;
import static net.openhft.chronicle.testframework.NetworkUtil.getAvailablePort;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;
import static net.openhft.chronicle.testframework.Waiters.waitForCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledOnOs(value = OS.MAC, disabledReason = "MacOS loopback strangeness causes intermittent failures")
class TcpProxyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpProxy.class);
    private static final int TIMEOUT_MS = 3_000;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        shutdownAndWaitForTermination(executorService);
    }

    @Test
    void testProxying() throws IOException {
        startServerAndProxyAnd((serverSocketChannel, tcpProxy) -> {
            connectViaProxyAnd(serverSocketChannel, tcpProxy, (clientChannel, serverChannel) -> {
                sendString(clientChannel, "hello");
                pause(100);
                assertEquals("hello", receiveString(serverChannel));
            });
        });
    }

    @Test
    void testCanSuspendTraffic() throws IOException {
        startServerAndProxyAnd((serverSocketChannel, tcpProxy) -> {
            connectViaProxyAnd(serverSocketChannel, tcpProxy, (clientChannel, serverChannel) -> {
                tcpProxy.stopForwardingTrafficAndPauseNewConnections();
                pause(100);
                sendString(clientChannel, "hello");
                pause(100);
                assertEquals("", receiveString(serverChannel));
            });
        });
    }

    @Test
    void testCanDropConnections() throws IOException {
        startServerAndProxyAnd(((serverSocketChannel, tcpProxy) -> {
            connectViaProxyAnd(serverSocketChannel, tcpProxy, (clientChannel, serverChannel) -> {
                tcpProxy.dropConnectionsAndPauseNewConnections();
                waitForCondition("server didn't disconnect", () -> atEndOfStream(serverChannel), TIMEOUT_MS);
                waitForCondition("client didn't disconnect", () -> atEndOfStream(clientChannel), TIMEOUT_MS);
            });
        }));
    }

    @Test
    void testCanReconnectAfterDroppingConnections() throws IOException {
        startServerAndProxyAnd(((serverSocketChannel, tcpProxy) -> {
            connectViaProxyAnd(serverSocketChannel, tcpProxy, (clientChannel, serverChannel) -> {
                tcpProxy.dropConnectionsAndPauseNewConnections();
                waitForCondition("server didn't disconnect", () -> atEndOfStream(serverChannel), TIMEOUT_MS);
                waitForCondition("client didn't disconnect", () -> atEndOfStream(clientChannel), TIMEOUT_MS);
            });

            tcpProxy.acceptNewConnections();
            connectViaProxyAnd(serverSocketChannel, tcpProxy, (clientChannel, serverChannel) -> {
                sendString(clientChannel, "hello");
                pause(100);
                assertEquals("hello", receiveString(serverChannel));
            });
        }));
    }

    private boolean atEndOfStream(SocketChannel socketChannel) {
        final ByteBuffer allocate = ByteBuffer.allocate(123);
        try {
            final int read = socketChannel.read(allocate);
            return read == -1;
        } catch (IOException e) {
            throw new IllegalStateException("Got IOException", e);
        }
    }

    private void sendString(SocketChannel channel, String string) throws IOException {
        final ByteBuffer helloBuffer = ByteBuffer.wrap(string.getBytes());
        channel.write(helloBuffer);
    }

    private String receiveString(SocketChannel channel) throws IOException {
        ByteBuffer recvBuf = ByteBuffer.allocate(128);
        channel.read(recvBuf);
        recvBuf.flip();
        return new String(recvBuf.array(), recvBuf.position(), recvBuf.remaining());
    }


    private void startServerAndProxyAnd(ServerAndProxyBody serverAndProxyConsumer) throws IOException {
        try (final ServerSocketChannel serverSocket = ServerSocketChannel.open().bind(new InetSocketAddress(0));
             final TcpProxy tcpProxy = new TcpProxy(getAvailablePort(), (InetSocketAddress) serverSocket.socket().getLocalSocketAddress(), executorService)) {
            LOGGER.info("Server listening on " + serverSocket.socket().getLocalSocketAddress());
            executorService.submit(tcpProxy);
            waitForCondition("TCP proxy didn't open", tcpProxy::isOpen, TIMEOUT_MS);
            serverSocket.configureBlocking(false);
            serverAndProxyConsumer.run(serverSocket, tcpProxy);
        }
    }

    private void connectViaProxyAnd(ServerSocketChannel serverSocket, TcpProxy tcpProxy, ConnectionTestBody ptb) throws IOException {
        try (final SocketChannel clientSocket = SocketChannel.open()) {
            waitForCondition("TCP proxy didn't open", tcpProxy::isOpen, TIMEOUT_MS);
            clientSocket.configureBlocking(false);
            final InetSocketAddress remote = tcpProxy.socketAddress();
            LOGGER.info("Client connecting to " + remote);
            clientSocket.connect(new InetSocketAddress("localhost", remote.getPort()));
            long endTime = System.currentTimeMillis() + TIMEOUT_MS;
            SocketChannel connection = serverSocket.accept();
            while (connection == null) {
                connection = serverSocket.accept();
                if (System.currentTimeMillis() > endTime) {
                    clientSocket.finishConnect();
                    throw new IllegalStateException("Didn't connect " + clientSocket.getRemoteAddress());
                }
            }
            LOGGER.info("connected");
            connection.configureBlocking(false);
            clientSocket.finishConnect();
            ptb.run(clientSocket, connection);
        }
    }

    private interface ConnectionTestBody {
        void run(SocketChannel clientSocketChannel, SocketChannel serverSocketChannel) throws IOException;
    }

    private interface ServerAndProxyBody {
        void run(ServerSocketChannel serverSocketChannel, TcpProxy proxy) throws IOException;
    }
}