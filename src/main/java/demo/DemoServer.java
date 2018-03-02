package demo;

import sgw.NettyGatewayServer;
import sgw.NettyGatewayServerConfig;
import sgw.ThreadPoolStrategy;
import sgw.core.http_channel.routing.Router;

public class DemoServer {

    public static void main(String[] args) {
        try {
            NettyGatewayServerConfig config = NettyGatewayServerConfig.getDebugConfig();
//            ThreadPoolStrategy strategy = new ThreadPoolStrategy(ThreadPoolStrategy.MULTI_WORKERS, 2, 0);
//            config.setThreadPoolStrategy(strategy);

            NettyGatewayServer server = new NettyGatewayServer(config);

//            Router router = server.getRouter();
//            FilterMngr.Instance.addFilters(Arrays.asList());


            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
