package sgw.core.http_channel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import sgw.core.ServerConfig;
import sgw.core.routing.Router;
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
    public static final String EDGE_HANDLER = "edge_handler";
    public static final String ROUTING = "routing";
    public static final String HTTP_AGGREGATOR = "http_aggregator";
    public static final String SERVICE_INVOKER = "service_invoker";
    public static final String RESPONSE_CONVERTOR = "response_convertor";
    public static final String CONVERTOR = "convertor";

    private ServerConfig config;

    // router is shared among all channels
    private Router router;
    private RpcInvokerDiscoverer discoverer;

    public HttpChannelInitializer(ServerConfig config) {
        this.config = config;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public void setDiscoverer(RpcInvokerDiscoverer discoverer) {
        this.discoverer = discoverer;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // initialize context
        HttpChannelContext chanCtx = new HttpChannelContext();
        chanCtx.setConfig(config);
        chanCtx.setRouter(router);
        chanCtx.setInvokerDiscoverer(discoverer);
        chanCtx.setHttpChannel(ch);

        int maxContentLength = config.getMaxHttpContentLength();
        ChannelPipeline p = ch.pipeline();
        // Handler order MATTERS.
        // codec
        p.addLast(HTTP_CODEC, new HttpServerCodec()); // HttpServerCodec is both inbound and outbound
        p.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(maxContentLength)); // inbound

        // edge
        p.addLast(EDGE_HANDLER, new HttpEdgeHandler(chanCtx)); // duplex

        // channel inbound handlers
        p.addLast(ROUTING, new HttpRoutingHandler(chanCtx)); // inbound
        p.addLast(CONVERTOR, new HttpConvertHandler(chanCtx)); // codec
//        p.addLast(SERVICE_INVOKER, new ServiceInvokeHandler(chanCtx));
        p.addLast(SERVICE_INVOKER, new InvokeHandler(chanCtx));

    }
}
