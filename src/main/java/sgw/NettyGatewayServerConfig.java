package sgw;

public class NettyGatewayServerConfig {

    public static final NettyGatewayServerConfig DEFAULT;
    private static final int defaultPort = 8080;

    static {
        DEFAULT = new NettyGatewayServerConfig(ThreadPoolStrategy.SINGLE_THREAD,
                0, 0);
    }

    private ThreadPoolStrategy tpStrategy;
    private int wThreads;
    private int bThreads;
    private int port;

    public NettyGatewayServerConfig(ThreadPoolStrategy strategy, int workerThreads, int backendThreads) {
        this(strategy, workerThreads, backendThreads, defaultPort);
    }

    /**
     *
     * @param workerThreads only necessary when using multi_worker or multi_worker_and_executor strategy
     * @param backendThreads only necessary when using multi_worker_and_executor strategy
     */
    public NettyGatewayServerConfig(ThreadPoolStrategy strategy, int workerThreads, int backendThreads, int port) {
        tpStrategy = strategy;
        wThreads = 1; bThreads = 1; wThreads = 1;

        if (strategy == ThreadPoolStrategy.MULTI_WORKERS) {
            wThreads = workerThreads;
            bThreads = workerThreads;
        }
        if (strategy == ThreadPoolStrategy.MULTI_WORKERS_AND_BACKENDS) {
            wThreads = workerThreads;
            bThreads = backendThreads;
        }

        this.port = port;
    }

    public boolean isSingleThread() {
        return tpStrategy == ThreadPoolStrategy.SINGLE_THREAD;
    }

    public boolean isMultiWorkers() {
        return tpStrategy == ThreadPoolStrategy.MULTI_WORKERS;
    }

    public int getWorkerThreads() {
        return wThreads;
    }

    public int getBackendThreads() {
        return bThreads;
    }

    public int getPort() {
        return port;
    }

}
