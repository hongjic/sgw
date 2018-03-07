package demo;

import sgw.NettyGatewayServer;
import sgw.NettyGatewayServerConfig;
import sgw.ThreadPoolStrategy;
import sgw.core.routing.Router;
import sgw.core.routing.RouterScanner;

public class DemoServer {

    public static void main(String[] args) {
        try {
            NettyGatewayServerConfig config = NettyGatewayServerConfig.getDebugConfig();
            ThreadPoolStrategy strategy = new ThreadPoolStrategy(ThreadPoolStrategy.MULTI_WORKERS, 16, 0);
            config.setThreadPoolStrategy(strategy);

            NettyGatewayServer server = new NettyGatewayServer(config);
            /**
             * init Router by scanning annotation
             */
            Router router = new RouterScanner()
                    .ofPackage("demo.parser")
                    .init();

//            /**
//             * init Router from routing.yaml
//             */
//            Router router = Router.initFromConfig();

            server.setRouter(router);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
