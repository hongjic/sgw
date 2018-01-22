package sgw.core.service_channel.thrift;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.ServiceChannelInitializer;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;

import java.net.SocketAddress;

/**
 * has to be thread-safe
 *
 */
public class ThriftNonblockingInvoker implements RpcInvoker {

    private Logger logger = LoggerFactory.getLogger(ThriftNonblockingInvoker.class);

    private RpcInvokerDef invokerDef;
    private SocketAddress remoteAddress;
    private Bootstrap bootstrap;
    private Channel thriftChannel;
    private Channel inboundChannel;
    private InvokerState state;

    public ThriftNonblockingInvoker(RpcInvokerDef invokerDef, SocketAddress remoteAddress) {
        this.invokerDef = invokerDef;
        this.remoteAddress = remoteAddress;
        setState(InvokerState.INACTIVE);
    }

    @Override
    public ThriftNonblockingInvoker register(EventLoopGroup group) {
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ServiceChannelInitializer(invokerDef, inboundChannel));
        return this;
    }

    @Override
    public ChannelFuture invokeAsync(Object param) {
        if (thriftChannel == null)
            throw new NullPointerException("thriftChannel");
        else if (!thriftChannel.isActive())
            throw new IllegalStateException("Method invoke() called before channel becomes active.");
        setState(InvokerState.INVOKED);
        return thriftChannel.writeAndFlush(param);
    }

    @Override
    public ChannelFuture connectAsync() {
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
        logger.info("Connecting remote: " + invokerDef.toSimpleString());
        thriftChannel = future.channel();
        return future;
    }

    private void connected() {
        logger.info("Connection established: " + invokerDef.toSimpleString());
        setState(InvokerState.ACTIVE);
    }

    @Override
    public Channel getRpcChannel() {
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

    @Override
    public RpcInvokerDef getInvokerDef() {
        return invokerDef;
    }
}
