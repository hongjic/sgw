package sgw.core.services;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import java.util.List;

/**
 * has to be thread-safe
 */
public interface RpcInvoker {

    enum InvokerState {
        INACTIVE, CONNECTING, ACTIVE, INVOKED, FINISHED;
    }

    ChannelFuture invoke(List<Object> params) throws Exception;

    ChannelFuture connect() throws Exception;

    Channel getChannel();

    RpcInvoker register(EventLoopGroup group);

    void setState(InvokerState state);

    InvokerState getState();

    void setInboundChannel(Channel channel);

}
