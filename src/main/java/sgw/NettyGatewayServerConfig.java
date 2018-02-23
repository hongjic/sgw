package sgw;

import java.util.HashMap;

public class NettyGatewayServerConfig extends HashMap<String, Object>{

    private static final String PORT = "port";
    private static final String THREAD_POOL_STRATEGY = "threadPoolStrategy";
    private static final String MAX_HTTP_CONTENT_LENGTH = "maxHttpContentLength";

    private static final int defaultPort = 8080;
    private static final int defaultMaxHttpContentLength = 1048576; /** 1MB **/

    private static NettyGatewayServerConfig config;

    public static NettyGatewayServerConfig getCurrentConfig() {
        return config;
    }

    public static NettyGatewayServerConfig getDebugConfig() {
        config = new NettyGatewayServerConfig();
        config.put(THREAD_POOL_STRATEGY, ThreadPoolStrategy.DEBUG_MODE);
        config.put(PORT, defaultPort);

        config.put(MAX_HTTP_CONTENT_LENGTH, defaultMaxHttpContentLength);
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

    public int getMaxHttpContentLength() {
        return (int) get(MAX_HTTP_CONTENT_LENGTH);
    }

}
