package sgw.demo;

import sgw.core.GatewayServer;
import sgw.core.ServerConfig;
import sgw.core.ThreadPoolStrategy;
import sgw.core.routing.Router;
import sgw.core.routing.RouterScanner;
import sgw.core.service_discovery.RpcInvokerDiscoverer;

public class DemoServer {

    public static void main(String[] args) {
        try {
            ServerConfig config = ServerConfig.useDebugConfig();
            ThreadPoolStrategy strategy = new ThreadPoolStrategy(ThreadPoolStrategy.MULTI_WORKERS, 1, 1);
            config.setThreadPoolStrategy(strategy);

            GatewayServer server = new GatewayServer(config);
            /**
             * init Router by scanning annotation
             */
            Router router = new RouterScanner()
                    .ofPackage("sgw.demo.parser")
                    .init();
            RpcInvokerDiscoverer discoverer = new RpcInvokerDiscoverer.Builder()
                    .loadFromConfig("demo/src/main/resources/discovery.properties")
                    .build("demo/src/main/resources/zookeeper.properties");

//            FilterMngr.Instance.addFilters(
//                    new ReceiveRequestCounter(),
//                    new SendResponseCounter()
//            );

//            /**
//             * init Router from routing.yaml
//             */
//            Router router = Router.initFromConfig();

            server.setRouter(router);
            server.setDiscoverer(discoverer);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
