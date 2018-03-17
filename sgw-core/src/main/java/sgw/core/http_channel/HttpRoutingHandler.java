package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.*;
import sgw.core.http_channel.util.ChannelOrderedHttpRequest;
import sgw.core.routing.UndefinedHttpRequestException;
import sgw.core.service_channel.RpcType;
import sgw.core.service_discovery.ServiceNode;
import sgw.core.util.FastMessage;
import sgw.core.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.service_discovery.ServiceUnavailableException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * {@link HttpRoutingHandler} first finds {@link RpcInvokerDef} according to the
 * registered {@link Router}, then gets the actual remote address (included in
 * {@link RpcInvoker}) using the service definition via {@link RpcInvokerDiscoverer}.
 */
public class HttpRoutingHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = LoggerFactory.getLogger(HttpRoutingHandler.class);

    private HttpChannelContext chanCtx;
    private Router router;
    private RpcInvokerDiscoverer invokerDiscoverer;
    private long channelRequestId;

    public HttpRoutingHandler(HttpChannelContext chanCtx) {
        this.chanCtx = chanCtx;
        this.router = chanCtx.getRouter();
        this.invokerDiscoverer = chanCtx.getInvokerDiscoverer();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /**
         * msg: {@link sgw.core.http_channel.util.ChannelOrderedHttpRequest}
         */
        assert (msg instanceof ChannelOrderedHttpRequest);
        ChannelOrderedHttpRequest request = (ChannelOrderedHttpRequest) msg;
        channelRequestId = request.channelMessageId();

        HttpRequestContext reqCtx = chanCtx.getRequestContext(channelRequestId);
        HttpMethod method = request.method();
        URI uri;
        try {
            uri = new URI(request.uri());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            ctx.channel().close();
            return;
        }

        logger.debug("Request {}: Http request received: {} {}", reqCtx.getGlobalRequestId(), method, uri.toString());

        // rpc invoker can be determined as soon as we get HttpRequestDef
        // no need to wait for the full request body arrives.
        HttpRequestDef httpRequestDef = new HttpRequestDef(request);
        Map<String, String> pathParams = httpRequestDef.getParams();

        RpcInvokerDef invokerDef = router.get(httpRequestDef);
        ServiceNode node = invokerDiscoverer.find(invokerDef.getServiceName());
        RpcInvoker invoker = RpcInvoker.create(invokerDef, node, chanCtx, reqCtx);
        logger.debug("Request {}: remote address {}", reqCtx.getGlobalRequestId(), invoker.toString());

        ConvertorInfo cinfo = Convertors.Cache.getConvertorInfo(invokerDef.getHttpConvertorClazzName());
        FullHttpRequestParser reqPar = new RequestParserImpl(cinfo, pathParams);
        FullHttpResponseGenerator resGen = new ResponseGeneratorImpl(cinfo);

        reqCtx.setInvokerDef(invokerDef);
        reqCtx.setHttpRequestParser(reqPar);
        reqCtx.setHttpResponseGenerator(resGen);
        reqCtx.setInvoker(invoker);

        /**
         * Send the channel_request_id to {@link sgw.core.http_channel.thrift.HttpReqToThrift} for default
         * To support other rpc protocols, use {@link RpcInvokerDef#getProtocol()} to check protocol and
         * replace `REQUEST_CONVERTOR` and `RESPONSE_CONVERTOR` handler.
         */
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ServiceUnavailableException) {
            cause.printStackTrace();
            FastMessage fm = new FastMessage(channelRequestId, (ServiceUnavailableException) cause);
            fm.send(ctx.channel(), chanCtx.getRequestContext(channelRequestId));
        }
        else if (cause instanceof UndefinedHttpRequestException) {
            cause.printStackTrace();
            FastMessage fm = new FastMessage(channelRequestId, (UndefinedHttpRequestException) cause);
            fm.send(ctx.channel(), chanCtx.getRequestContext(channelRequestId));
        }
        else {
            ctx.fireExceptionCaught(cause);
        }
    }

}
