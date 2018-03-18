package sgw.core.pool;

import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelPool;
import sgw.core.ServerConfig;
import sgw.core.service_channel.ServiceChannelInitializer;
import sgw.core.service_discovery.ServiceNode;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * shared among threads
 */
public enum ChannelPoolMngr {

    Instance;

    private Map<ServiceNode, ChannelPool> poolMap = new HashMap<>();

    /**
     *
     * @param node service node
     * @return return the shared channel pool, create a new one if not found.
     */
    public ChannelPool pool(ServiceNode node) {
        if (poolMap.containsKey(node))
            return poolMap.get(node);
        else {
            synchronized (this) {
                if (poolMap.containsKey(node))
                    return poolMap.get(node);
                SocketAddress address = node.remoteAddress();
                ServerConfig config = ServerConfig.config();
                int maxChannels = config.getMaxChannelPerChannelPool();
                EventLoop executor = config.getThreadPoolStrategy().getBackendGroup().next();
                ChannelPool pool = new MultiplexedChannelPool(
                        address,
                        maxChannels,
                        executor,
                        new ServiceChannelInitializer(node.protocol())
                );
                poolMap.put(node, pool);
                return pool;
            }
        }
    }

    /**
     * remove a channel pool. usually we should close the removed channel pool after it is removed.
     * @param node service node
     * @return the removed channel pool, null if not found.
     */
    public ChannelPool removePool(ServiceNode node) {
        ChannelPool pool = poolMap.get(node);
        if (pool != null)
            pool.close();
        return poolMap.remove(node);
    }

}
