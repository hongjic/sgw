package sgw;

import sgw.core.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyGatewayServer {

    private NioEventLoopGroup acceptor;
    private NioEventLoopGroup workerGroup;
    private NioEventLoopGroup backendGroup;

    private int serverPort;

    public NettyGatewayServer() {
        this(NettyGatewayServerConfig.DEFAULT);
    }

    /**
     *
     * @param config configuration for thread pool strategy.
     */
    public NettyGatewayServer(NettyGatewayServerConfig config) {
        serverPort = config.getPort();

        acceptor = new NioEventLoopGroup(1);
        if (config.isSingleThread()) {
            workerGroup = acceptor;
            backendGroup = acceptor;
        }
        else {
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads());
            if (config.isMultiWorkers()) {
                backendGroup = workerGroup;
            }
            else {
                // ThreadPoolStrategy.MULTI_WORKERS_AND_BACKENDS
                backendGroup = new NioEventLoopGroup(config.getBackendThreads());
            }
        }
    }

    public void start() throws Exception {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptor, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(serverPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            System.out.println("server shuting down...");
            backendGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            acceptor.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        NettyGatewayServer server = new NettyGatewayServer();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
