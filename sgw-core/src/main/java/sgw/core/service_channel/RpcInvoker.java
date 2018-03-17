package sgw.core.service_channel;

import io.netty.channel.ChannelFuture;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.service_channel.thrift.ThriftInvoker;
import sgw.core.service_discovery.ServiceNode;
import sgw.core.util.ChannelOrderedMessage;

/**
 * has to be thread-safe
 */
public interface RpcInvoker<I extends ChannelOrderedMessage, O extends ChannelOrderedMessage> {

    enum InvokerState {
        INACTIVE, CONNECTING, CONNECT_FAIL, ACTIVE, INVOKED, INVOKE_FAIL, SUCCESS, TIMEOUT, FAIL
    }

    /**
     * Send rpc message asynchronously to the rpc channel
     */
    ChannelFuture invokeAsync(I param);

    /**
     * This method is invoked by the rpc channel when the request has been completed or failed.
     * Telling the http channel to handle the invokeResult put in http channel context.
     */
    void handleResult(O result);

    /**
     * Return the invoker state.
     * @return the current invoker state.
     */
    InvokerState getState();

    void setState(InvokerState state);

    static RpcInvoker create(RpcInvokerDef invokerDef,
                             ServiceNode node,
                             HttpChannelContext chanCtx,
                             HttpRequestContext reqCtx) {
        switch (invokerDef.getProtocol()) {
            case thrift: return new ThriftInvoker(invokerDef, node, chanCtx, reqCtx);
            default: return null;
        }
    }

}
