package sgw.core.http_channel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.NettyGatewayServerConfig;
import sgw.core.filters.PostRoutingFiltersHandler;
import sgw.core.filters.PreRoutingFiltersHandler;
import sgw.core.routing.Router;
import sgw.core.http_channel.thrift.HttpReqToThrift;
import sgw.core.http_channel.thrift.ThriftToHttpRsp;
import sgw.core.service_discovery.RpcInvokerDiscoverer;

import java.util.concurrent.atomic.AtomicLong;

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
    public static final String PRE_FILTER = "pre_filter";
    public static final String POST_FILTER = "post_filter";
    public static final String ROUTING = "routing";
    public static final String HTTP_AGGREGATOR = "http_aggregator";
    public static final String REQUEST_CONVERTOR = "request_convertor";
    public static final String SERVICE_INVOKER = "service_invoker";
    public static final String RESPONSE_CONVERTOR = "response_convertor";

    private NettyGatewayServerConfig config;

    // router is shared among all channels
    private Router router;
    private RpcInvokerDiscoverer discoverer;

    public HttpChannelInitializer(NettyGatewayServerConfig config) {
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
        HttpChannelContext httpCtx = new HttpChannelContext();
        httpCtx.setConfig(config);
        httpCtx.setRouter(router);
        httpCtx.setInvokerDiscoverer(discoverer);
        httpCtx.setHttpChannel(ch);
        httpCtx.setSendFastMessage(false);

        int maxContentLength = config.getMaxHttpContentLength();
        ChannelPipeline p = ch.pipeline();
        // codec
        p.addLast(HTTP_CODEC, new HttpServerCodec()); // HttpServerCodec is both inbound and outbound

        // channel outbound handlers
        p.addLast(POST_FILTER, new PostRoutingFiltersHandler(httpCtx)); // filter handler
        p.addLast(RESPONSE_CONVERTOR, new ThriftToHttpRsp(httpCtx));

        // channel inbound handlers
        p.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(maxContentLength));
        p.addLast(PRE_FILTER, new PreRoutingFiltersHandler(httpCtx)); // filter handler
        p.addLast(ROUTING, new HttpRoutingHandler(httpCtx));
        p.addLast(REQUEST_CONVERTOR, new HttpReqToThrift(httpCtx));
        p.addLast(SERVICE_INVOKER, new ServiceInvokeHandler(httpCtx));
    }
}
