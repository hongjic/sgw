package sgw.core;

import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolStrategy {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolStrategy.class);

    public static final int SINGLE_THREAD = 0;

    /**
     * MULTI_WORKERS might be the best way to do this. You can share the http
     * channel and the corresponding rpc channel in the same {@link io.netty.channel.EventLoop}
     * (a thread). So things can run efficiently without potential context switching.
     */
    public static final int MULTI_WORKERS = 1;
    public static final int MULTI_WORKERS_AND_BACKENDS = 2;

    public static ThreadPoolStrategy DEBUG_MODE = new ThreadPoolStrategy(SINGLE_THREAD, 0, 0);


    private int strategy;
    private int workerThreads;
    private int backendThreads;

    private NioEventLoopGroup acceptor;
    private NioEventLoopGroup workerGroup;
    private NioEventLoopGroup backendGroup;

    /**
     *
     * @param strategy single_thread, multi_workers or multi_workers_and_backends
     * @param workerThreads 0 means using default (Netty internally use CPU*2)
     * @param backendThreads 0 means using default
     */
    public ThreadPoolStrategy(int strategy, int workerThreads, int backendThreads) throws IllegalStateException{
        if (strategy < 0 || strategy > 2 || workerThreads < 0 || backendThreads < 0) {
            IllegalStateException e = new IllegalStateException("Invalid thread pool strategy.");
            logger.error(e.getMessage());
            throw e;
        }
        this.strategy = strategy;
        this.workerThreads = workerThreads;
        this.backendThreads = backendThreads;
    }

    public void createThreadPool() {
        acceptor = new NioEventLoopGroup(1);
        if (isSingleThread()) {
            workerGroup = acceptor;
            backendGroup = acceptor;
            logger.info("Server using single thread: [1]");
        }
        else {
            // if w == 0, that means using default_event_loop_threads, which is CPU*2
            workerGroup = new NioEventLoopGroup(workerThreads);
            if (isMultiWorkers()) {
                backendGroup = workerGroup;
                logger.info("Server using multi workers: [1, {}]", workerThreads);
            }
            else {
                // ThreadPoolStrategy.MULTI_WORKERS_AND_BACKENDS
                backendGroup = new NioEventLoopGroup(backendThreads);
                logger.info("Server using multi workers multi backends: [1, {}, {}]",
                        workerThreads, backendThreads);
            }
        }
    }

    public boolean isSingleThread() {
        return strategy == SINGLE_THREAD;
    }

    public boolean isMultiWorkers() {
        return strategy == MULTI_WORKERS;
    }

    public boolean isMultiWorkersBackends() {
        return strategy == MULTI_WORKERS_AND_BACKENDS;
    }

    public NioEventLoopGroup getAcceptor() {
        return acceptor;
    }

    public NioEventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public NioEventLoopGroup getBackendGroup() {
        return backendGroup;
    }

}
