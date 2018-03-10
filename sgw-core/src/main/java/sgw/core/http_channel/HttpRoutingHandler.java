package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.*;
import sgw.core.routing.UndefinedHttpRequestException;
import sgw.core.util.FastMessage;
import sgw.core.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.service_discovery.ServiceUnavailableException;
import sgw.core.util.RequestCounter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link HttpRoutingHandler} first finds {@link RpcInvokerDef} according to the
 * registered {@link Router}, then gets the actual remote address (included in
 * {@link RpcInvoker}) using the service definition via {@link RpcInvokerDiscoverer}.
 */
public class HttpRoutingHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = LoggerFactory.getLogger(HttpRoutingHandler.class);

    private HttpChannelContext httpCtx;
    private final AtomicLong count = new AtomicLong(0);

    public HttpRoutingHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        count.incrementAndGet();
        httpCtx.put("routing_handler_start", System.currentTimeMillis());
        httpCtx.setRequestId(RequestCounter.Instance.incrementAndGet());
        Router router = httpCtx.getRouter();
        // init service detector
        RpcInvokerDiscoverer invokerDetector = httpCtx.getInvokerDiscoverer();

        /**
         * msg type: {@link FullHttpRequest}
         */
        HttpRequest request = (HttpRequest) msg;
        HttpMethod method = request.method();
        URI uri;
        try {
            uri = new URI(request.uri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ctx.channel().close();
            return;
        }

        logger.debug("Request {}: Http request received: {} {}", httpCtx.getRequestId(), method, uri.toString());

        // rpc invoker can be determined as soon as we get HttpRequestDef
        // no need to wait for the full request body arrives.
        HttpRequestDef httpRequestDef = new HttpRequestDef(request);
        Map<String, String> pathParams = httpRequestDef.getParams();

        RpcInvokerDef invokerDef = router.get(httpRequestDef);
        RpcInvoker invoker = invokerDetector.find(invokerDef);
        logger.debug("Request {}: remote address {}", httpCtx.getRequestId(), invoker.toString());

        ConvertorInfo cinfo = Convertors.Cache.getConvertorInfo(invokerDef.getHttpConvertorClazzName());
        FullHttpRequestParser reqPar = new RequestParserImpl(cinfo, pathParams);
        FullHttpResponseGenerator resGen = new ResponseGeneratorImpl(cinfo);

        httpCtx.setInvokerDef(invokerDef);
        httpCtx.setFullHttpRequestParser(reqPar);
        httpCtx.setFullHttpResponseGenerator(resGen);
        httpCtx.setInvoker(invoker);

        /**
         * Send to {@link sgw.core.http_channel.thrift.HttpReqToThrift} for default
         * To support other rpc protocols, use {@link RpcInvokerDef#getProtocol()} to check protocol and
         * replace `REQUEST_CONVERTOR` and `RESPONSE_CONVERTOR` handler.
         */
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ServiceUnavailableException) {
            cause.printStackTrace();
            FastMessage fm = new FastMessage((ServiceUnavailableException) cause);
            fm.send(ctx, httpCtx).addListener(ChannelFutureListener.CLOSE);
        }
        else if (cause instanceof UndefinedHttpRequestException) {
            cause.printStackTrace();
            FastMessage fm = new FastMessage((UndefinedHttpRequestException) cause);
            fm.send(ctx, httpCtx).addListener(ChannelFutureListener.CLOSE);
        }
        else {
            ctx.fireExceptionCaught(cause);
        }
    }

}
