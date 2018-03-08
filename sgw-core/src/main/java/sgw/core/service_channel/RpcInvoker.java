package sgw.core.service_channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.ServiceInvokeHandler;

/**
 * has to be thread-safe
 */
public interface RpcInvoker {

    enum InvokerState {
        INACTIVE, CONNECTING, CONNECT_FAIL, ACTIVE, INVOKED, SUCCESS, TIMEOUT, FAIL
    }

    /**
     * @param group The eventloop group that the rpc channel will use.
     * @return self
     */
    RpcInvoker register(EventLoopGroup group, HttpChannelContext httpCtx);

    /**
     * Connect to remote service
     * This method will create the actual rpc channel
     */
    ChannelFuture connectAsync();

    /**
     * Send rpc request to remote service.
     */
    ChannelFuture invokeAsync(Object param);

    /**
     * This method is invoked by the rpc channel when the request has been completed or failed.
     * Telling the http channel to handle the invokeResult put in http channel context.
     */
    void handleResult(Object reuslt, RpcChannelContext rpcCtx);

    /**
     * Bind the http channel to RpcInvoker
     * @param channel http channel to bind
     */
    void setInboundChannel(Channel channel);

    Channel getRpcChannel();

    /**
     * Return the invoker state.
     * @return the current invoker state.
     */
    InvokerState getState();

    void setState(InvokerState state);

}
