package sgw.core.service_discovery.zookeeper;

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
