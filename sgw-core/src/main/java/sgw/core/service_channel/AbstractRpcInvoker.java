package sgw.core.service_channel;

import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.service_discovery.ServiceNode;
import sgw.core.util.ChannelOrderedMessage;

public abstract class AbstractRpcInvoker<I extends ChannelOrderedMessage, O extends ChannelOrderedMessage>
        implements RpcInvoker<I, O> {

    protected RpcInvokerDef invokerDef;
    protected ServiceNode serviceNode;
    protected HttpChannelContext httpChanCtx;
    protected HttpRequestContext httpReqCtx;
    protected volatile InvokerState state;

    public AbstractRpcInvoker(RpcInvokerDef invokerDef,
                              ServiceNode serviceNode,
                              HttpChannelContext httpChanCtx,
                              HttpRequestContext httpReqCtx) {
        this.invokerDef = invokerDef;
        this.serviceNode = serviceNode;
        this.httpChanCtx = httpChanCtx;
        this.httpReqCtx = httpReqCtx;
        setState(InvokerState.INACTIVE);
    }

    @Override
    public void setState(InvokerState state) {
        this.state = state;
    }

    @Override
    public InvokerState getState() {
        return state;
    }

}
