package sgw.core.service_channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import sgw.core.http_channel.ServiceInvokeHandler;

/**
 * has to be thread-safe
 */
public interface RpcInvoker {

    enum InvokerState {
        INACTIVE, CONNECTING, ACTIVE, INVOKED, FINISHED;
    }

    /**
     * @param group The eventloop group that the rpc channel will use.
     * @return self
     */
    RpcInvoker register(EventLoopGroup group, ServiceInvokeHandler invokeHandler);

    ChannelFuture connectAsync();

    ChannelFuture invokeAsync(Object param);

    void receiveResult(Object result);

    void setState(InvokerState state);

    void setInboundChannel(Channel channel);

    Channel getRpcChannel();

    InvokerState getState();

}
