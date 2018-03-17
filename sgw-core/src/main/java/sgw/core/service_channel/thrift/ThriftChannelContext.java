package sgw.core.service_channel.thrift;

import sgw.core.http_channel.HttpRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * No synchronization is needed since all ops on a single channel runs in the same thread.
 */
public class ThriftChannelContext {

    private final Map<Long, ThriftRequestContext> channelRequests = new HashMap<>();
    private long channelRequestCounter = 0; // the number of requests received

    public ThriftChannelContext() { }

    public ThriftRequestContext newRequestContext(HttpRequestContext httpReqCtx) {
        long channelReqId = ++ channelRequestCounter;
        ThriftRequestContext reqCtx = new ThriftRequestContext();
        reqCtx.setRpcChRequestId(channelReqId);
        reqCtx.setHttpGlRequestId(httpReqCtx.getGlobalRequestId());
        reqCtx.setHttpChRequestId(httpReqCtx.getChannelRequestId());
        reqCtx.setRpcInvoker(httpReqCtx.getInvoker());
        reqCtx.setRpcInvokerDef(httpReqCtx.getInvokerDef());

        channelRequests.put(channelReqId, reqCtx);
        return reqCtx;
    }

    public void removeRequestContext(long channelReqId) {
        channelRequests.remove(channelReqId);
    }

    public ThriftRequestContext getRequestContext(long channelRequestId) {
        return channelRequests.get(channelRequestId);
    }

}
