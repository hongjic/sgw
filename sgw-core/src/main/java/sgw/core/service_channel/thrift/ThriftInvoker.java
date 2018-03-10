package sgw.core.service_channel.thrift;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.ProxyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.service_channel.RpcChannelContext;
import sgw.core.service_channel.ServiceChannelInitializer;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;

import java.net.SocketAddress;

/**
 * has to be thread-safe
 *
 */
public class ThriftInvoker implements RpcInvoker {

    private Logger logger = LoggerFactory.getLogger(ThriftInvoker.class);

    private RpcInvokerDef invokerDef;
    private SocketAddress remoteAddress;
    private Bootstrap bootstrap;
    private Channel thriftChannel;
    private Channel inboundChannel;
    private InvokerState state;
    private HttpChannelContext httpCtx;

    public ThriftInvoker(RpcInvokerDef invokerDef, SocketAddress remoteAddress) {
        this.invokerDef = invokerDef;
        this.remoteAddress = remoteAddress;
        setState(InvokerState.INACTIVE);
    }

    @Override
    public ThriftInvoker register(EventLoopGroup group, HttpChannelContext httpCtx) {
        if (bootstrap == null) {
            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .handler(new ServiceChannelInitializer(invokerDef, this, httpCtx.getRequestId()))
                    .option(ChannelOption.AUTO_READ, false);
        }
        this.httpCtx = httpCtx;
        bootstrap.group(group);
        return this;
    }

    @Override
    public ChannelFuture invokeAsync(Object param) {
        if (thriftChannel == null)
            throw new NullPointerException("thriftChannel");
        else if (!thriftChannel.isActive())
            throw new IllegalStateException("Method invokeAsync() called before channel becomes active.");
        // acutual write op will be executed in the thread where the thrift channel belongs.
        ChannelFuture future = thriftChannel.writeAndFlush(param);
        setState(InvokerState.INVOKED);
        return future;
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
    public void handleResult(Object result, RpcChannelContext rpcCtx) {
        EventLoop inboundEventLoop = inboundChannel.eventLoop();
        if (inboundEventLoop.inEventLoop())
            handleResult0(result, rpcCtx);
        else
            inboundEventLoop.execute(() -> handleResult0(result, rpcCtx));
    }

    private void handleResult0(Object result, RpcChannelContext rpcCtx) {
        httpCtx.setRpcChannelContext(rpcCtx);
        httpCtx.setInvokeResult(result);
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

    @Override
    public String toString() {
        return remoteAddress.toString();
    }

}
