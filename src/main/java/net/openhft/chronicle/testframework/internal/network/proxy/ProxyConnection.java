package net.openhft.chronicle.testframework.internal.network.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import static net.openhft.chronicle.testframework.CloseableUtil.closeQuietly;
import static net.openhft.chronicle.testframework.ThreadUtil.pause;

public class ProxyConnection implements Closeable, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyConnection.class);

    private final SocketChannel inboundChannel;
    private final InetSocketAddress remoteAddress;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private volatile boolean running = true;
    private volatile boolean finished = false;
    private volatile boolean forwardingTraffic = true;

    public ProxyConnection(SocketChannel inboundChannel, InetSocketAddress remoteAddress) {
        this.inboundChannel = inboundChannel;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void run() {
        running = true;
        try (final SocketChannel outboundChannel = SelectorProvider.provider().openSocketChannel()) {
            outboundChannel.configureBlocking(true);
            outboundChannel.connect(remoteAddress);
            LOGGER.info("Established connection between {} and {}",
                    inboundChannel.socket().getRemoteSocketAddress(), outboundChannel.socket().getRemoteSocketAddress());
            outboundChannel.configureBlocking(false);
            inboundChannel.configureBlocking(false);
            while (running) {
                if (forwardingTraffic) {
                    relayTraffic(inboundChannel, outboundChannel);
                    relayTraffic(outboundChannel, inboundChannel);
                } else {
                    pause(10);
                }
            }
            LOGGER.info("Terminating connection between {} and {}",
                    inboundChannel.socket().getRemoteSocketAddress(), outboundChannel.socket().getRemoteSocketAddress());
        } catch (IOException e) {
            LOGGER.error("Connection failed", e);
        } finally {
            closeQuietly(inboundChannel);
        }
        finished = true;
    }

    private void relayTraffic(SocketChannel sourceChannel, SocketChannel destinationChannel) throws IOException {
        byteBuffer.clear();
        int read = sourceChannel.read(byteBuffer);
        if (read > 0) {
            byteBuffer.flip();
            destinationChannel.write(byteBuffer);
        }
    }

    @Override
    public void close() throws IllegalStateException {
        running = false;
        while (!finished) {
            pause(10);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void stopForwardingTraffic() {
        forwardingTraffic = false;
    }
}
