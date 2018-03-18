package sgw.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;

public class ServerConfig extends HashMap<String, Object>{

    private static final String PORT = "port";
    private static final String THREAD_POOL_STRATEGY = "thread_pool_strategy";
    private static final String MAX_HTTP_CONTENT_LENGTH = "max_http_content_length";
    private static final String MAX_REQUEST_PER_HTTP_CONNECTION = "max_request_per_http_connection";
    private static final String MAX_CHANNEL_PER_CHANNEL_POOL = "max_channel_per_channel_pool";

    private static final int defaultPort = 8080;
    private static final int defaultMaxHttpContentLength = 1048576; // 1MB
    private static final int defaultMaxRequestPerHttpConnection = -1; // <=0 means unlimited
    private static final int defaultMaxChannelPerChannelPool = 10;

    private static ServerConfig config;

    public static ServerConfig config() {
        return config;
    }

    public static ServerConfig useDebugConfig() {
        config = new ServerConfig();
        config.setThreadPoolStrategy(ThreadPoolStrategy.DEBUG_MODE);
        config.setPort(defaultPort);
        config.setMaxHttpContentLength(defaultMaxHttpContentLength);
        config.setMaxRequestPerHttpConnection(defaultMaxRequestPerHttpConnection);
        config.setMaxChannelPerChannelPool(defaultMaxChannelPerChannelPool);
        return config;
    }

    public ServerConfig() {
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
        set(THREAD_POOL_STRATEGY, strategy);
    }

    public int getPort() {
        return (int) get(PORT);
    }

    public void setPort(int port) {
        set(PORT, port);
    }

    public int getMaxHttpContentLength() {
        return (int) get(MAX_HTTP_CONTENT_LENGTH);
    }

    public void setMaxHttpContentLength(int length) {
        set(MAX_HTTP_CONTENT_LENGTH, length);
    }

    public int getMaxRequestPerHttpConnection() {
        return (int) get(MAX_REQUEST_PER_HTTP_CONNECTION);
    }

    public void setMaxRequestPerHttpConnection(int maxRequests) {
        set(MAX_REQUEST_PER_HTTP_CONNECTION, maxRequests);
    }

    public int getMaxChannelPerChannelPool() {
        return (int) get(MAX_CHANNEL_PER_CHANNEL_POOL);
    }

    public void setMaxChannelPerChannelPool(int maxChannels) {
        set(MAX_CHANNEL_PER_CHANNEL_POOL, maxChannels);
    }

    @Override
    public Object put(String key, Object value) {
        throw new NotImplementedException();
    }

    private Object set(String key, Object value) {
        return super.put(key, value);
    }

}
