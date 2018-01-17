package sgw.core;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.NettyGatewayServerConfig;
import sgw.core.routing.Router;
import sgw.core.routing.RouterGenerator;
import sgw.core.routing.RouterGeneratorFactory;
import sgw.core.services.RpcInvokerManager;
import sgw.core.services.thrift.ThriftServiceManager;

/**
 * Only created once during server bootstrap.
 * Whenever a new http request comes in, it uses the same {@link HttpChannelInitializer} instance.
 * This gurantees {@link Router} and {@link RpcInvokerManager} are also created once.
 *
 * Shared among threads, need to be thread-safe.
 */
@ChannelHandler.Sharable
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Logger logger = LoggerFactory.getLogger(HttpChannelInitializer.class);

    private Router router;
    private RpcInvokerManager invokerManager;

    public HttpChannelInitializer(NettyGatewayServerConfig config) {
        try {
            RouterGenerator routerGenerator = new RouterGeneratorFactory(config.getRouterDataSource()).create();
            router = routerGenerator.generate();
            // TODO: initialize RpcInvokerManager according to configuration
            invokerManager = new ThriftServiceManager();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // HttpServerCodec is both inbound and outbound
        ch.pipeline().addLast("codec", new HttpServerCodec());

        // ChannelInboundHandler
        HttpServerHandler handler = new HttpServerHandler();
        handler.useRouter(router);
        handler.useInvokerManager(invokerManager);
        ch.pipeline().addLast("server", handler);
    }
}
