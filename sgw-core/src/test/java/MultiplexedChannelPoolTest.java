import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import org.junit.Test;
import sgw.core.pool.MultiplexedChannelPool;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MultiplexedChannelPoolTest {

    // use a random local port for testing.
    private final SocketAddress serverAddress = new InetSocketAddress("localhost", 58932);
    private final int connections = 1000;
    private final int maxChannels = 5;

    @Test
    public void testConcurrent() {
        EventLoopGroup group1 = new NioEventLoopGroup(), group2 = new NioEventLoopGroup();

        final Set<ChannelId> channelIds = new HashSet<>();
        try {
            final ServerBootstrap b = new ServerBootstrap();
            final Thread thread = new Thread(() -> {
                b.group(group1)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                System.out.println("server channel init." + ch.isActive());
                            }
                        })
                        .bind(serverAddress);
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                }
            });
            thread.start(); // start local server for testing
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }

            EventLoopGroup group = group2;
            // channel pool
            ChannelPool channelPool = new MultiplexedChannelPool(
                    serverAddress, maxChannels, group,
                    new ChannelPoolHandler() {
                        @Override
                        public void channelReleased(Channel ch) { /** do nothing. **/ }

                        @Override
                        public void channelAcquired(Channel ch) { /** do nothing. **/ }

                        @Override
                        public void channelCreated(Channel ch) {
                            System.out.println("channel created." + ch.isActive());
                        }
                    }
            );
            final AtomicInteger connects = new AtomicInteger(0);
            for (int i = 0; i < connections; i++) {
                Future<Channel> future = channelPool.acquire();
                future.addListener((Future<Channel> future1) -> {
                    if (future1.isSuccess()) {
                        ChannelId cid = future1.get().id();
                        channelIds.add(cid);
                    } else {
                        fail("connection failed.");
                    }
                    if (connects.incrementAndGet() == connections)
                        thread.interrupt();
                });
            }
            thread.join();
            assertEquals(maxChannels, channelIds.size());
            channelPool.close();
            Thread.sleep(100);

        }
        catch (Exception e) {}
        finally {
            group1.shutdownGracefully();
            group2.shutdownGracefully();
        }
    }
}
