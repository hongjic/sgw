package sgw.core.http_channel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.NettyGatewayServerConfig;
import sgw.core.http_channel.routing.Router;
import sgw.core.http_channel.routing.RouterGenerator;
import sgw.core.http_channel.routing.RouterGeneratorFactory;
import sgw.core.service_channel.RpcInvokerDetector;

/**
 * Only created once during server bootstrap.
 * Whenever a new http request comes in, it uses the same {@link HttpChannelInitializer} instance.
 * This gurantees {@link Router} and {@link RpcInvokerDetector} are also created once.
 *
 * Shared among threads, need to be thread-safe.
 */
@ChannelHandler.Sharable
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final String HTTP_CODEC = "http_codec";
    public static final String SERVICE_DISCOVERY = "service_discovery";
    public static final String HTTP_AGGREGATOR = "http_aggregator";
    public static final String HTTP_TO_PARAM = "http_to_param";
    public static final String SERVICE_INVOKER = "service_invoker";
    public static final String RESULT_TO_HTTP = "result_to_http";

    private final Logger logger = LoggerFactory.getLogger(HttpChannelInitializer.class);

    private HttpChannelContext httpCtx;

    public HttpChannelInitializer(NettyGatewayServerConfig config) throws Exception{
        httpCtx = new HttpChannelContext();
        httpCtx.setConfig(config);
        // init router
        RouterGenerator routerGenerator = new RouterGeneratorFactory(config.getRouterDataSource()).create();
        httpCtx.setRouter(routerGenerator.generate());
        logger.info("Router initialized.");
    }

    @Override
    public void initChannel(SocketChannel ch) {
        httpCtx.setHttpChannel(ch);
        ChannelPipeline p = ch.pipeline();
        // HttpServerCodec is both inbound and outbound
        p.addLast(HTTP_CODEC, new HttpServerCodec());
        p.addLast(RESULT_TO_HTTP, new ResultHttpConvertor(httpCtx));

        // ChannelInboundHandler
        p.addLast(SERVICE_DISCOVERY, new HttpRoutingHandler(httpCtx));

        int maxContentLength = NettyGatewayServerConfig.getCurrentConfig().getMaxHttpContentLength();
        p.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(maxContentLength));

        p.addLast(HTTP_TO_PARAM, new HttpParamConvertor(httpCtx));
        p.addLast(SERVICE_INVOKER, new ServiceInvokeHandler(httpCtx));
    }
}
