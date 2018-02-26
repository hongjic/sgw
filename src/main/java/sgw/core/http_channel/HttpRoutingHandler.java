package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.Convertors;
import sgw.core.http_channel.routing.UndefinedHttpRequestException;
import sgw.core.util.FastMessage;
import sgw.core.http_channel.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.service_discovery.ServiceUnavailableException;
import sgw.core.util.FastMessageSender;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link HttpRoutingHandler} first finds {@link RpcInvokerDef} according to the
 * registered {@link Router}, then gets the actual remote address (included in
 * {@link RpcInvoker}) using the service definition via {@link RpcInvokerDiscoverer}.
 */
public class HttpRoutingHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = LoggerFactory.getLogger(HttpRoutingHandler.class);

    private HttpChannelContext httpCtx;

    public HttpRoutingHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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

        logger.info("Receive Http Request: {} {}", method, uri.toString());

        // rpc invoker can be determined as soon as we get HttpRequestDef
        // no need to wait for the full request body arrives.
        HttpRequestDef httpRequestDef = new HttpRequestDef(request);

        RpcInvokerDef invokerDef = router.get(httpRequestDef);
        RpcInvoker invoker = invokerDetector.find(invokerDef);

        FullHttpRequestParser reqPar = Convertors.Cache.getReqParser(invokerDef.getRequestParser());
        FullHttpResponseGenerator resGen = Convertors.Cache.getResGen(invokerDef.getResponseGenerator());

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
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("Channel read complete.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ServiceUnavailableException) {
            cause.printStackTrace();
            httpCtx.setSendFastMessage(true);
            ChannelFuture future = FastMessageSender.send(ctx, new FastMessage((ServiceUnavailableException) cause));
            future.addListener(ChannelFutureListener.CLOSE);
        }
        else if (cause instanceof UndefinedHttpRequestException) {
            cause.printStackTrace();
            httpCtx.setSendFastMessage(true);
            ChannelFuture future = FastMessageSender.send(ctx, new FastMessage((UndefinedHttpRequestException) cause));
            future.addListener(ChannelFutureListener.CLOSE);
        }
        else {
            ctx.fireExceptionCaught(cause);
        }
    }

}
