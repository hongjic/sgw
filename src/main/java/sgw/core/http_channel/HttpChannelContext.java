package sgw.core.http_channel;

import io.netty.channel.Channel;
import sgw.NettyGatewayServerConfig;
import sgw.core.http_channel.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDetector;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;

/**
 * share data between handlers in the same httpChannel.
 * this class will never be shared among threads.
 */
public final class HttpChannelContext {

    private Router router;
    private RpcInvokerDetector invokerDetector;
    private RpcInvoker invoker;
    private FullHttpRequestParser requestParser;
    private FullHttpResponseGenerator responseGenerator;
    private Object invokeParam;
    private NettyGatewayServerConfig config;
    private Channel httpChannel;

    public Router getRouter() {
        return router;
    }

    public RpcInvokerDetector getInvokerDetector() {
        return invokerDetector;
    }

    public void setInvokerDetector(RpcInvokerDetector invokerDetector) {
        this.invokerDetector = invokerDetector;
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

    public Object getInvokeParam() {
        return invokeParam;
    }

    public void setInvokeParam(Object invokeParam) {
        this.invokeParam = invokeParam;
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
}
