package sgw.core;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.NettyGatewayServerConfig;
import sgw.core.routing.Router;
import sgw.core.routing.RouterDataSource;
import sgw.core.routing.RouterGenerator;
import sgw.core.routing.RouterGeneratorFactory;
import sgw.core.services.RpcInvokerDetector;
import sgw.core.services.RpcInvokerDetectorFactory;

/**
 * Only created once during server bootstrap.
 * Whenever a new http request comes in, it uses the same {@link HttpChannelInitializer} instance.
 * This gurantees {@link Router} and {@link RpcInvokerDetector} are also created once.
 *
 * Shared among threads, need to be thread-safe.
 */
@ChannelHandler.Sharable
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Logger logger = LoggerFactory.getLogger(HttpChannelInitializer.class);

    private Router router;
    private RpcInvokerDetector invokerDetector;

    public HttpChannelInitializer(NettyGatewayServerConfig config) throws Exception{
        initRouter(config.getRouterDataSource());
        initServiceDetector(config);
    }

    private void initRouter(RouterDataSource source) throws Exception {
        RouterGenerator routerGenerator = new RouterGeneratorFactory(source).create();
        router = routerGenerator.generate();
    }

    private void initServiceDetector(NettyGatewayServerConfig config) {
        invokerDetector = new RpcInvokerDetectorFactory(config).create();
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // HttpServerCodec is both inbound and outbound
        ch.pipeline().addLast("codec", new HttpServerCodec());

        // ChannelInboundHandler
        HttpServerHandler handler = new HttpServerHandler();
        handler.useRouter(router);
        handler.useInvokerManager(invokerDetector);
        ch.pipeline().addLast("server", handler);
    }
}
