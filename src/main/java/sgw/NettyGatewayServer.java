package sgw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyGatewayServer {

    private final Logger logger = LoggerFactory.getLogger(NettyGatewayServer.class);

    private NioEventLoopGroup acceptor;
    private NioEventLoopGroup workerGroup;
    private NioEventLoopGroup backendGroup;

    private int serverPort;
    private HttpChannelInitializer httpChannelInitializer;

    /**
     *
     * @param config configuration for thread pool strategy.
     */
    public NettyGatewayServer(NettyGatewayServerConfig config) throws Exception {
        serverPort = config.getPort();
        try {
            httpChannelInitializer = new HttpChannelInitializer(config);
        } catch (Exception e) {
            logger.error("HttpChannel initialization failed.");
            throw e;
        }

        acceptor = new NioEventLoopGroup(1);
        if (config.isSingleThread()) {
            workerGroup = acceptor;
            backendGroup = acceptor;
            logger.info("Server using single thread: [1]");
        }
        else {
            int w = config.getWorkerThreads();
            // if w == 0, that means using default_event_loop_threads, which is CPU*2
            workerGroup = new NioEventLoopGroup(w);
            if (config.isMultiWorkers()) {
                backendGroup = workerGroup;
                logger.info("Server using multi workers: [1, {}]", w);
            }
            else {
                // ThreadPoolStrategy.MULTI_WORKERS_AND_BACKENDS
                int b = config.getBackendThreads();
                backendGroup = new NioEventLoopGroup(b);
                logger.info("Server using multi workers multi backends: [1, {}, {}]", w, b);
            }
        }
    }

    public void start() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptor, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(httpChannelInitializer)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(serverPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            logger.info("server shutting down...");
            backendGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            acceptor.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            NettyGatewayServer server = new NettyGatewayServer(NettyGatewayServerConfig.DEBUG);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
