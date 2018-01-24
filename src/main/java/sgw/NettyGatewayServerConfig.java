package sgw;

import sgw.core.http_channel.routing.RouterDataSource;

import java.util.HashMap;

public class NettyGatewayServerConfig extends HashMap<String, Object>{

    private static final String PORT = "port";
    private static final String ROUTER_DATA_SOURCE = "routerDataSource";
    private static final String THREAD_POOL_STRATEGY = "threadPoolStrategy";
    private static final String MAX_HTTP_CONTENT_LENGTH = "maxHttpContentLength";
    private static final String SERVICE_DISCOVERY_IMPL = "serviceDiscoveryImpl";

    private static final int defaultPort = 8080;
    private static final String defaultRouterPropertiesFilePath = "src/main/resources/routing.properties";
    private static final int defaultMaxHttpContentLength = 1048576; /** 1MB **/
    private static final String defaultServiceDiscoveryImpl = "zookeeper";

    private static NettyGatewayServerConfig config;

    public static NettyGatewayServerConfig getCurrentConfig() {
        return config;
    }

    public static NettyGatewayServerConfig getDebugConfig() {
        config = new NettyGatewayServerConfig();
        config.put(THREAD_POOL_STRATEGY, ThreadPoolStrategy.DEBUG_MODE);
        config.put(PORT, defaultPort);

        RouterDataSource source = new RouterDataSource(RouterDataSource.Type.PROPERTIES_FILE);
        source.setPropertiesFilePath(defaultRouterPropertiesFilePath);
        config.put(ROUTER_DATA_SOURCE, source);
        config.put(MAX_HTTP_CONTENT_LENGTH, defaultMaxHttpContentLength);
        config.put(SERVICE_DISCOVERY_IMPL, defaultServiceDiscoveryImpl);
        return config;
    }

    public NettyGatewayServerConfig() {
        super();
    }

    public boolean isSingleThread() {
        return getThreadPoolStrategy().isSingleThread();
    }

    public boolean isMultiWorkers() {
        return getThreadPoolStrategy().isMultiWorkers();
    }

    public ThreadPoolStrategy getThreadPoolStrategy() {
        return (ThreadPoolStrategy) get(THREAD_POOL_STRATEGY);
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

    public int getMaxHttpContentLength() {
        return (int) get(MAX_HTTP_CONTENT_LENGTH);
    }

    public String getServiceDiscoveryImpl() {
        return (String) get(SERVICE_DISCOVERY_IMPL);
    }

    public void setServiceDiscoveryImpl(String impl) {
        put(SERVICE_DISCOVERY_IMPL, impl);
    }
}
