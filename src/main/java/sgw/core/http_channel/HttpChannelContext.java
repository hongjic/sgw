package sgw.core.http_channel;

import io.netty.channel.Channel;
import sgw.NettyGatewayServerConfig;
import sgw.core.http_channel.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;

/**
 * share data between handlers in the same httpChannel.
 * this class will never be shared among threads.
 */
public final class HttpChannelContext {

    private Router router;
    private RpcInvokerDiscoverer invokerDiscoverer;
    private RpcInvoker invoker;
    private RpcInvokerDef invokerDef;
    private FullHttpRequestParser requestParser;
    private FullHttpResponseGenerator responseGenerator;
    private NettyGatewayServerConfig config;
    private Channel httpChannel;

    public Router getRouter() {
        return router;
    }

    public RpcInvokerDiscoverer getInvokerDiscoverer() {
        return invokerDiscoverer;
    }

    public void setInvokerDiscoverer(RpcInvokerDiscoverer invokerDiscoverer) {
        this.invokerDiscoverer = invokerDiscoverer;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public RpcInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    public FullHttpRequestParser getFullHttpRequestParser() {
        return requestParser;
    }

    public void setFullHttpRequestParser(FullHttpRequestParser parser) {
        this.requestParser = parser;
    }

    public NettyGatewayServerConfig getConfig() {
        return config;
    }

    public void setConfig(NettyGatewayServerConfig config) {
        this.config = config;
    }

    public Channel getHttpChannel() {
        return httpChannel;
    }

    public void setHttpChannel(Channel httpChannel) {
        this.httpChannel = httpChannel;
    }

    public FullHttpResponseGenerator getResponseGenerator() {
        return responseGenerator;
    }

    public void setFullHttpResponseGenerator(FullHttpResponseGenerator responseGenerator) {
        this.responseGenerator = responseGenerator;
    }

    public void setInvokerDef(RpcInvokerDef invokerDef) {
        this.invokerDef = invokerDef;
    }

    public RpcInvokerDef getInvokerDef() {
        return invokerDef;
    }
}
