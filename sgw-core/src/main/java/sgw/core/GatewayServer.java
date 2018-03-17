package sgw.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import sgw.core.routing.Router;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.util.Args;

public class GatewayServer {

    private final Logger logger = LoggerFactory.getLogger(GatewayServer.class);

    private NioEventLoopGroup acceptor;
    private NioEventLoopGroup workerGroup;
    private NioEventLoopGroup backendGroup;

    private int serverPort;
    private HttpChannelInitializer httpChannelInitializer;
    private Router router;
    private RpcInvokerDiscoverer discoverer;
    private ServerConfig config;

    /**
     *
     * @param config configuration for thread pool strategy.
     */
    public GatewayServer(ServerConfig config) throws Exception {
        this.config = config;
        serverPort = config.getPort();
        ThreadPoolStrategy strategy = config.getThreadPoolStrategy();
        strategy.createThreadPool();
        acceptor = strategy.getAcceptor();
        workerGroup = strategy.getWorkerGroup();
        backendGroup = strategy.getBackendGroup();
    }

    public Router getRouter() {
        return router;
    }

    public void setDiscoverer(RpcInvokerDiscoverer discoverer) {
        this.discoverer = discoverer;
    }

    public void setRouter(Router router) {
        if (this.router != null) {
            // this is necessary to resolve circuler reference.
            this.router.clear();
        }
        this.router = router;
    }

    public void start() throws Exception {
        try {
            Args.notNull(router, "router");
            Args.notNull(discoverer, "discoverer");
        } catch (Exception e) {
            logger.error("Server start fail.");
            throw e;
        }
        httpChannelInitializer = new HttpChannelInitializer(config);
        httpChannelInitializer.setRouter(router);
        httpChannelInitializer.setDiscoverer(discoverer);
        try {
            discoverer.start();
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptor, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(httpChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128);

            ChannelFuture f = b.bind(serverPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            close();
        }
    }

    public void close() throws InterruptedException {
        logger.info("server shutting down...");
        backendGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();
        acceptor.shutdownGracefully().sync();
        discoverer.close();
    }

}
