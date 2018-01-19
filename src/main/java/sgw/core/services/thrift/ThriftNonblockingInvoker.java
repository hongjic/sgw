package sgw.core.services.thrift;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.BackendChannelInitializer;
import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvokerDef;

import java.net.SocketAddress;
import java.util.List;

/**
 * has to be thread-safe
 *
 */
public class ThriftNonblockingInvoker implements RpcInvoker {

    private Logger logger = LoggerFactory.getLogger(ThriftNonblockingInvoker.class);

    private RpcInvokerDef thriftInvokerDef;
    private SocketAddress remoteAddress;
    private Bootstrap bootstrap;
    private Channel thriftChannel;
    private Channel inboundChannel;
    private InvokerState state;

    public ThriftNonblockingInvoker(RpcInvokerDef invokerDef, SocketAddress remoteAddress) {
        this.thriftInvokerDef = invokerDef;
        this.remoteAddress = remoteAddress;
        setState(InvokerState.INACTIVE);
    }

    @Override
    public ThriftNonblockingInvoker register(EventLoopGroup group) {
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new BackendChannelInitializer(thriftInvokerDef));
        return this;
    }

    @Override
    public ChannelFuture invoke(List<Object> params) {
        if (thriftChannel == null)
            throw new NullPointerException("thriftChannel");
        else if (!thriftChannel.isActive())
            throw new IllegalStateException("Method invoke() called before channel becomes active.");

        setState(InvokerState.INVOKED);
        return thriftChannel.write(params);
    }

    @Override
    public ChannelFuture connect() {
        ChannelFuture future = bootstrap.connect(remoteAddress);
        future.addListener((ChannelFuture future1) -> {
            EventLoop eventLoop = inboundChannel.eventLoop();
            if (eventLoop.inEventLoop())
                connected();
            else
                eventLoop.execute(() ->
                        connected());
        });
        setState(InvokerState.CONNECTING);
        logger.info("Connecting remote: " + thriftInvokerDef.toSimpleString());
        thriftChannel = future.channel();
        return future;
    }

    private void connected() {
        logger.info("Connection established: " + thriftInvokerDef.toSimpleString());
        setState(InvokerState.ACTIVE);
    }

    @Override
    public Channel getChannel() {
        return thriftChannel;
    }

    @Override
    public void setState(InvokerState state) {
        this.state = state;
    }

    @Override
    public InvokerState getState() {
        return state;
    }

    @Override
    public void setInboundChannel(Channel channel) {
        inboundChannel = channel;
    }
}
