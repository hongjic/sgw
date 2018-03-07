package sgw.core.service_channel.thrift;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.ServiceInvokeHandler;
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
    private ServiceInvokeHandler invokeHandler;

    public ThriftNonblockingInvoker(RpcInvokerDef invokerDef, SocketAddress remoteAddress) {
        this.invokerDef = invokerDef;
        this.remoteAddress = remoteAddress;
        setState(InvokerState.INACTIVE);
    }

    @Override
    public ThriftNonblockingInvoker register(EventLoopGroup group, ServiceInvokeHandler invokeHandler) {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .handler(new ServiceChannelInitializer(invokerDef, this));
        }
        bootstrap.group(group);
        this.invokeHandler = invokeHandler;
        return this;
    }

    @Override
    public ChannelFuture invokeAsync(Object param) {
        if (thriftChannel == null)
            throw new NullPointerException("thriftChannel");
        else if (!thriftChannel.isActive())
            throw new IllegalStateException("Method invokeAsync() called before channel becomes active.");
        setState(InvokerState.INVOKED);
        // acutual write op will be executed in the thread where the thrift channel belongs.
        return thriftChannel.writeAndFlush(param);
    }

    @Override
    public ChannelFuture connectAsync() {
        if (bootstrap == null)
            throw new IllegalStateException("Method connectAsync() called before eventloop registration.");
        ChannelFuture future = bootstrap.connect(remoteAddress);
        future.addListener((ChannelFuture future1) -> {
            EventLoop eventLoop = inboundChannel.eventLoop();
            if (eventLoop.inEventLoop())
                ifConnected(future1);
            else
                eventLoop.execute(() ->
                        ifConnected(future1));
        });
        setState(InvokerState.CONNECTING);
        logger.debug("Connecting remote: " + invokerDef.toString());
        thriftChannel = future.channel();
        return future;
    }

    @Override
    public void handleResult(Object result) {
        EventLoop inboundEventLoop = inboundChannel.eventLoop();
        if (inboundEventLoop.inEventLoop())
            handleResult0(result);
        else
            inboundEventLoop.execute(() -> handleResult0(result));
    }

    private void handleResult0(Object result) {
        invokeHandler.receiveResult(result);
        inboundChannel.pipeline().fireChannelWritabilityChanged();
    }

    private void ifConnected(ChannelFuture future) {
        if (future.isSuccess()) {
            logger.debug("Connection established: " + invokerDef.toString());
            setState(InvokerState.ACTIVE);
        }
        else {
            logger.debug("Connection failure: " + invokerDef.toString());
            setState(InvokerState.CONNECT_FAIL);
        }
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
}
