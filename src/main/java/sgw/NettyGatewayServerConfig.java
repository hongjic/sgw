package sgw;

import sgw.core.routing.RouterDataSource;

import java.util.HashMap;

public class NettyGatewayServerConfig extends HashMap<String, Object>{

    private static final String PORT = "port";
    private static final String ROUTER_DATA_SOURCE = "routerDataSource";
    private static final String THREAD_POOL_STRATEGY = "threadPoolStrategy";

    private static final int defaultPort = 8080;
    private static final String defaultRouterPropertiesFilePath = "src/main/resources/routing.properties";

    public static NettyGatewayServerConfig DEBUG;

    static {
        DEBUG = new NettyGatewayServerConfig();
        DEBUG.put(THREAD_POOL_STRATEGY, ThreadPoolStrategy.DEBUG_MODE);
        DEBUG.put(PORT, defaultPort);

        RouterDataSource source = new RouterDataSource(RouterDataSource.Type.PROPERTIES_FILE);
        source.setPropertiesFilePath(defaultRouterPropertiesFilePath);
        DEBUG.put(ROUTER_DATA_SOURCE, source);
    }

    public NettyGatewayServerConfig() {
        super();
    }

    public boolean isSingleThread() {
        return ((ThreadPoolStrategy) get(THREAD_POOL_STRATEGY)).isSingleThread();
    }

    public boolean isMultiWorkers() {
        return ((ThreadPoolStrategy) get(THREAD_POOL_STRATEGY)).isMultiWorkers();
    }

    public int getWorkerThreads() {
        return ((ThreadPoolStrategy) get(THREAD_POOL_STRATEGY)).getWorkerThreads();
    }

    public int getBackendThreads() {
        return ((ThreadPoolStrategy) get(THREAD_POOL_STRATEGY)).getBackendThreads();
    }

    public void setThreadPoolStrategy(ThreadPoolStrategy strategy) {
        put(THREAD_POOL_STRATEGY, strategy);
    }

    public int getPort() {
        return (int) get(PORT);
    }

    public void setPort(int port) {
        put(PORT, port);
    }

    public void setRouterDataSource(RouterDataSource source) {
        put(ROUTER_DATA_SOURCE, source);
    }

    public RouterDataSource getRouterDataSource() {
        return (RouterDataSource) get(ROUTER_DATA_SOURCE);
    }
}
