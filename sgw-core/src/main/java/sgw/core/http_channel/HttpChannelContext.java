package sgw.core.http_channel;

import io.netty.channel.Channel;
import sgw.core.ServerConfig;
import sgw.core.routing.Router;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.util.RequestCounter;

import java.util.HashMap;
import java.util.Map;

/**
 * No synchronization is needed since all ops on a single channel runs in the same thread.
 */
public final class HttpChannelContext {

    private Router router;
    private RpcInvokerDiscoverer discoverer;
    private ServerConfig serverConfig;
    private Channel httpChannel;


    public Router getRouter() {
        return router;
    }

    public RpcInvokerDiscoverer getInvokerDiscoverer() {
        return discoverer;
    }

    public void setInvokerDiscoverer(RpcInvokerDiscoverer invokerDiscoverer) {
        this.discoverer = invokerDiscoverer;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public ServerConfig getConfig() {
        return serverConfig;
    }

    public void setConfig(ServerConfig config) {
        this.serverConfig = config;
    }

    public Channel getHttpChannel() {
        return httpChannel;
    }

    public void setHttpChannel(Channel httpChannel) {
        this.httpChannel = httpChannel;
    }

    private final Map<Long, HttpRequestContext> channelRequests = new HashMap<>();
    private long channelRequestCounter = 0; // the number of requests received

    HttpRequestContext newRequestContext() {
        long globalReqId = RequestCounter.Instance.incrementAndGet();
        long channelReqId = ++ channelRequestCounter;
        HttpRequestContext reqCtx = new HttpRequestContext(globalReqId, channelReqId);
        channelRequests.put(channelReqId, reqCtx);
        return reqCtx;
    }

    void removeRequestContext(long channelReqId) {
        channelRequests.remove(channelReqId);
    }

    HttpRequestContext getRequestContext(long channelRequestId) {
        return channelRequests.get(channelRequestId);
    }

}
