package sgw.core.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.net.SocketAddress;

/**
 * A {@link io.netty.channel.pool.ChannelPool} implementation that corresponds to a specific
 * remote server.
 *
 * Each channel handles one request at a time, the channel will be release back to the channel pool
 * only after the current request is finished.
 *
 * {@link ServerNodeChannelPool} will make sure the acquired channel is registered to the current
 * event loop when {@link #acquire()} is called and the acquired channel is ready to use.
 */
public class ServerNodeChannelPool implements ChannelPool {

    private String serviceName;
    private String nodeName;
    private Bootstrap bootstrap;
    private FixedChannelPool channelPool;

    public ServerNodeChannelPool(String serviceName,
                                 String nodeName,
                                 int maxConnections,
                                 EventLoopGroup eventExecutor,
                                 SocketAddress remoteAddress,
                                 final ChannelPoolHandler handler) {
        Bootstrap bs = new Bootstrap()
                .channel(NioSocketChannel.class)
                .remoteAddress(remoteAddress)
                .option(ChannelOption.AUTO_READ, false)
                .group(eventExecutor);
        this.bootstrap = bs;
        this.channelPool = new FixedChannelPool(bs, handler, maxConnections) {
            @Override
            public ChannelFuture connectChannel(Bootstrap b) {
                return null;
            }
        };
        this.serviceName = serviceName;
        this.nodeName = nodeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getNodeName() {
        return nodeName;
    }

    @Override
    public Future<Channel> acquire(Promise<Channel> promise) {
        return channelPool.acquire(promise);
    }

    @Override
    public Future<Channel> acquire() {
        return channelPool.acquire();
    }

    @Override
    public Future<Void> release(Channel channel) {
        return channelPool.release(channel);
    }

    @Override
    public Future<Void> release(Channel channel, Promise<Void> promise) {
        return channelPool.release(channel, promise);
    }

    @Override
    public void close() {
        channelPool.close();
    }

    private static class DefaultChannelPoolHandler implements ChannelPoolHandler {

        @Override
        public void channelCreated(Channel ch) {
            ChannelPipeline p = ch.pipeline();
        }

        @Override
        public void channelAcquired(Channel ch) {

        }

        @Override
        public void channelReleased(Channel ch) {

        }
    }



}
