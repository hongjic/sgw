package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcInvokerDetector;
import sgw.parser.FullHttpRequestParser;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link HttpRoutingHandler} first finds {@link RpcInvokerDef} according to the
 * registered {@link Router}, then gets the actual remote address (included in
 * {@link RpcInvoker}) using the service definition via {@link RpcInvokerDetector}.
 * Finally, it connects to the remote peer, creates a new {@link Channel} and
 * send the parsed Http request to that backend channel for further process.
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
        RpcInvokerDetector invokerDetector = httpCtx.getInvokerDetector();

        /**
         * msg types:
         * {@link HttpRequest}
         * {@link HttpContent}
         */
        if (msg instanceof HttpRequest) {
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

            logger.info("HttpRequest Method : {}", method);
            logger.info("HttpRequest URI: {}", uri.toString());

            // rpc invoker can be determined as soon as we get HttpRequestDef
            // no need to wait for the full request body arrives.
            HttpRequestDef httpRequestDef = new HttpRequestDef(request);
            RpcInvokerDef invokerDef = router.getRpcInvokerDef(httpRequestDef);

            try {
                /**
                 * According to invokerDef, bind the right {@link sgw.parser.FullHttpRequestParser} to
                 * {@link HttpParamConvertor} handler.
                 */
                /**
                 * TODO: initialize frequently used data convertors and save them in a pool for reusement.
                 * Because reflection is expensive and data convertors are stateless, should be reused.
                 */
                Class clazz = Class.forName(invokerDef.getParamConvertor());
                FullHttpRequestParser parser = (FullHttpRequestParser) clazz.newInstance();
                httpCtx.setFullHttpRequestParser(parser);
            } catch (ClassNotFoundException e) {
                logger.error("Cannot find param convertor defined for remote service: {}", invokerDef.toString());
                e.printStackTrace();
                ctx.channel().close();
            }

            // TODO: Async find. Upon success, call ServiceInvokerHandler.connectAndInvoke if invokeParam has been set.
            // temporarily get remote service synchronously.
            RpcInvoker invoker = invokerDetector.find(invokerDef);
            httpCtx.setInvoker(invoker);
        }

        // send to Http aggregator handler.
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("Channel read complete.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.channel().close();
    }

}
