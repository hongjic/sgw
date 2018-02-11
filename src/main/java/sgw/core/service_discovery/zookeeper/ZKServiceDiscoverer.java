package sgw.core.service_discovery.zookeeper;

import io.netty.channel.ChannelFuture;
import sgw.core.service_channel.RpcType;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.service_channel.thrift.ThriftNonblockingInvoker;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * Only created once, need to be thread-safe.
 */
public class ZKServiceDiscoverer implements RpcInvokerDiscoverer {

    private List<String> serviceNames;

    public ZKServiceDiscoverer(List<String> serviceNames) {
        this.serviceNames = serviceNames;
        // TODO: init singleton Curator client.
    }

    public void start() {
        // TODO: start listener and Curator TreeCache, start sync in background.
    }

    @Override
    public List<String> getAllServices() {
        return serviceNames;
    }

    @Override
    public ChannelFuture connectAsync(RpcInvokerDef invokerDef) {
        // TODO: connect service discovery asynchronously.
        return null;
    }

    @Override
    public ChannelFuture findAsync(RpcInvokerDef invokerDef) {
        // TODO: call serivice discovery asynchronously.
        return null;
    }

    /**
     * TODO: use findAsync and connectAsync
     */
    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) throws Exception {
        RpcType rpcProtocol = invokerDef.getProtocol();

        switch (rpcProtocol) {
            case Thrift: {
                SocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 9090);
                return new ThriftNonblockingInvoker(invokerDef, remoteAddress);
            }
            default: return null;
        }
    }
}
