package sgw.core.http_channel;

import sgw.core.http_channel.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDetector;
import sgw.parser.FullHttpRequestParser;

/**
 * share data between handlers in the same channel.
 */
public final class HttpChannelContext {

    private Router router;
    private RpcInvokerDetector invokerDetector;
    private RpcInvoker invoker;
    private FullHttpRequestParser parser;
    private Object invokeParam;

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
        return parser;
    }

    public void setFullHttpRequestParser(FullHttpRequestParser parser) {
        this.parser = parser;
    }

    public Object getInvokeParam() {
        return invokeParam;
    }

    public void setInvokeParam(Object invokeParam) {
        this.invokeParam = invokeParam;
    }
}
