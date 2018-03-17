package sgw.core.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.http.annotation.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.load_balancer.DynamicLoadBalancer;
import sgw.core.load_balancer.UnsafeRRDynamicLoadBalancer;
import sgw.core.util.Args;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Unlike {@link io.netty.channel.pool.FixedChannelPool}, this implementatino of {@link ChannelPool}
 * enable netty client to reuse channel even before the previous request has been completed.
 * Use this implementation when you want to share channel among simultaneous requests,
 * for example: multiplexing.
 *
 * Similar with {@link io.netty.channel.pool.FixedChannelPool}, all operations(acquire) on the
 * pooled channels are executed in the same {@link EventLoop}, so no synchronization is needed.
 *
 */
@ThreadSafe
public class MultiplexedChannelPool implements ChannelPool {

    private static final IllegalStateException POOL_ClOSED_ON_ACQUIRE =
            new IllegalStateException("MultiplexedChannel Pool is already closed.");

    private static final Map<ChannelOption, Object> OPTIONS = new HashMap<>();

    static {
        OPTIONS.put(ChannelOption.SO_KEEPALIVE, true);
        OPTIONS.put(ChannelOption.AUTO_READ, true);
    }

    private final Logger logger = LoggerFactory.getLogger(MultiplexedChannelPool.class);

    private final DynamicLoadBalancer lb;
    private int maxSize;
    private Bootstrap bootstrap;
    private EventLoop eventExecutor;
    private boolean closed;
    private SocketAddress remoteAddress;

    public MultiplexedChannelPool(SocketAddress remoteAddress,
                                  int maxSize,
                                  EventLoopGroup executor,
                                  ChannelInitializer<SocketChannel> initializer) {
        this(remoteAddress, maxSize, executor, executor, initializer);
    }

    public MultiplexedChannelPool(SocketAddress remoteAddress,
                                  int maxSize,
                                  EventLoopGroup channelExecutor,
                                  EventLoopGroup eventExecutor,
                                  ChannelInitializer<SocketChannel> initializer) {
        this(remoteAddress, maxSize, channelExecutor, eventExecutor, NioSocketChannel.class, initializer);
    }

    public MultiplexedChannelPool(SocketAddress remoteAddress,
                                  int maxSize,
                                  EventLoopGroup channelExecutor,
                                  EventLoopGroup eventExecutor,
                                  Class<? extends Channel> channelClass,
                                  ChannelInitializer<SocketChannel> initializer) {
        this(remoteAddress, maxSize, channelExecutor, eventExecutor, channelClass, new HashMap<>(), initializer);
    }

    public MultiplexedChannelPool(SocketAddress remoteAddress,
                                  int maxSize,
                                  EventLoopGroup channelExecutor,
                                  EventLoopGroup eventExecutor,
                                  Class<? extends Channel> channelClass,
                                  Map<ChannelOption, Object> options,
                                  ChannelInitializer<SocketChannel> initializer) {
        if (maxSize <= 0)
            throw new IllegalArgumentException("maxSize should be bigger than 0.");
        this.maxSize = maxSize;
        this.remoteAddress = remoteAddress;
        this.eventExecutor = eventExecutor.next();
        this.lb = new UnsafeRRDynamicLoadBalancer<>();
        this.bootstrap = new Bootstrap()
                .remoteAddress(remoteAddress)
                .channel(channelClass)
                .group(channelExecutor)
                .handler(initializer);
        for (Map.Entry<ChannelOption, Object> entry: overrideOptions(options).entrySet()) {
            this.bootstrap.option(entry.getKey(), entry.getValue());
        }
        closed = false;
    }

    /**
     * Bootstrap a new {@link Channel}. The default implementation uses {@link Bootstrap#connect()}, sub-classes may
     * override this.
     * <p>
     * The {@link Bootstrap} that is passed in here is cloned via {@link Bootstrap#clone()}, so it is safe to modify.
     */
    protected ChannelFuture connectChannel(Bootstrap bootstrap) {
        return bootstrap.connect();
    }

    @Override
    public Future<Channel> acquire() {
        return acquire(bootstrap.config().group().next().newPromise());
    }

    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {
        Args.notNull(promise, "promise");
        if (eventExecutor.inEventLoop())
            acquireHealthyFromPoolOrNew(promise);
        else
            eventExecutor.execute(() -> acquireHealthyFromPoolOrNew(promise));
        return promise;
    }

    private Future<Channel> acquireHealthyFromPoolOrNew(final Promise<Channel> promise) {
        try {
            assert lb.size() <= maxSize;
            if (lb.size() == maxSize) {
                Object ele = lb.next();
                if (ele instanceof Channel)
                    doHealthCheck((Channel) ele, promise);
                else {
                    ((Promise<Channel>) ele).addListener((Future<Channel> future) -> {
                        if (future.isSuccess()) {
                            promise.trySuccess(future.getNow());
                        }
                        else
                            promise.tryFailure(future.cause());
                    });
                }
                return promise;
            }
            else {
                Bootstrap bs = bootstrap.clone();
                ChannelFuture f = connectChannel(bs);
                lb.add(promise);
                if (f.isDone()) {
                    notifyConnect(f, promise);
                } else {
                    f.addListener((ChannelFuture f1) -> notifyConnect(f1, promise));
                }
            }
            return promise;
        } catch (Throwable cause) {
            promise.tryFailure(cause);
        }
        return promise;
    }

    private void doHealthCheck(final Channel ch, final Promise<Channel> promise) {
        if (eventExecutor.inEventLoop())
            doHealthCheck0(ch, promise);
        else
            eventExecutor.execute(() -> doHealthCheck0(ch, promise));
    }

    private void doHealthCheck0(final Channel ch, final Promise<Channel> promise) {
        assert eventExecutor.inEventLoop();

        if (closed)
            promise.tryFailure(POOL_ClOSED_ON_ACQUIRE);
        if (ch.isActive()) {
            try {
                promise.trySuccess(ch);
            } catch (Throwable cause) {
                closeAndFail(ch, cause, promise);
            }
        } else {
            closeChannel(ch);
            acquireHealthyFromPoolOrNew(promise);
        }
    }

    private void notifyConnect(ChannelFuture future, Promise<Channel> promise) {
        if (eventExecutor.inEventLoop())
            notifyConnect0(future, promise);
        else
            eventExecutor.execute(() -> notifyConnect0(future, promise));
    }

    private void notifyConnect0(ChannelFuture future, Promise<Channel> promise) {
        assert eventExecutor.inEventLoop();

        if (future.isSuccess()) {
            Channel channel = future.channel();
            if (closed)
                promise.tryFailure(POOL_ClOSED_ON_ACQUIRE);
            else if (promise.trySuccess(channel)) {
                lb.remove(promise);
                lb.add(channel);
            }
        }
        else {
            promise.tryFailure(future.cause());
            lb.remove(promise);
        }
    }

    private void closeChannel(Channel channel) {
        assert eventExecutor.inEventLoop();
        lb.remove(channel);
        channel.close().addListener((f) -> logger.debug("{} channel closed.", remoteAddress.toString()));

    }

    private void closeChannelPromise(Promise<Channel> promise) {
        assert eventExecutor.inEventLoop();
        lb.remove(promise);
        promise.tryFailure(POOL_ClOSED_ON_ACQUIRE);
    }

    private void closeAndFail(Channel channel, Throwable cause, Promise<?> promise) {
        assert eventExecutor.inEventLoop();
        closeChannel(channel);
        promise.tryFailure(cause);
    }

    @Override
    public Future<Void> release(Channel channel) {
        throw new NotImplementedException();
    }

    @Override
    public Future<Void> release(Channel channel, Promise<Void> promise) {
        throw new NotImplementedException();
    }

    @Override
    public void close() {
        if (eventExecutor.inEventLoop())
            close0();
        else
            eventExecutor.execute(() -> close0());
    }

    private void close0() {
        assert eventExecutor.inEventLoop();

        if (closed) return; // avoid multi entrance.
        closed = true;
        Iterator iter = lb.iterator();
        while (iter.hasNext()) {
            Object ele = iter.next();
            if (ele instanceof Channel) {
                if (((Channel) ele).isActive())
                    closeChannel((Channel) ele);
            }
            else {
                closeChannelPromise((Promise<Channel>) ele);
            }
        }
    }

    private static Map<ChannelOption, Object> overrideOptions(Map<ChannelOption, Object> options) {
        Map<ChannelOption, Object> map = new HashMap<>(OPTIONS);
        for (Map.Entry<ChannelOption, Object> entry: options.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
