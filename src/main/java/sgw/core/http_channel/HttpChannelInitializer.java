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
import sgw.core.http_channel.routing.RouterGeneratorFactory;
import sgw.core.service_discovery.RpcInvokerDiscoverer;

/**
 * Only created once during server bootstrap.
 * Whenever a new http request comes in, it uses the same {@link HttpChannelInitializer} instance.
 * This gurantees {@link Router} and {@link RpcInvokerDiscoverer} are also created once.
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

    private NettyGatewayServerConfig config;

    // router is shared among all channels
    private Router router;
    private RpcInvokerDiscoverer discoverer;

    public HttpChannelInitializer(NettyGatewayServerConfig config) throws Exception {
        this.config = config;
        initRouter();
        initDiscoverer();
    }

    private void initRouter() throws Exception {
        router = new RouterGeneratorFactory(config.getRouterDataSource()).create().generate();
        logger.info("Router initialized.");
    }

    private void initDiscoverer() throws Exception {
        discoverer = new RpcInvokerDiscoverer.Builder().loadFromConfig().build();
        // start listening and auto sync metadata changes.
        discoverer.start();
        logger.info("Discoverer initialized.");
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // initialize context
        HttpChannelContext httpCtx = new HttpChannelContext();
        httpCtx.setConfig(config);
        httpCtx.setRouter(router);
        httpCtx.setInvokerDiscoverer(discoverer);
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
