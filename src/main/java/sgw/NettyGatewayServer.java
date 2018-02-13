package sgw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelInitializer;
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
        ThreadPoolStrategy strategy = config.getThreadPoolStrategy();
        strategy.createThreadPool();
        acceptor = strategy.getAcceptor();
        workerGroup = strategy.getWorkerGroup();
        backendGroup = strategy.getBackendGroup();

        try {
            httpChannelInitializer = new HttpChannelInitializer(config);
        } catch (Exception e) {
            logger.error("Server Initialization failed.");
            throw e;
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
            NettyGatewayServerConfig config = NettyGatewayServerConfig.getDebugConfig();
            ThreadPoolStrategy strategy = new ThreadPoolStrategy(ThreadPoolStrategy.MULTI_WORKERS, 2, 0);
            config.setThreadPoolStrategy(strategy);

            NettyGatewayServer server = new NettyGatewayServer(config);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
