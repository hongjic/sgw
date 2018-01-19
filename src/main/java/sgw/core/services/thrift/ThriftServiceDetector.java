package sgw.core.services.thrift;

import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvokerDef;
import sgw.core.services.RpcInvokerDetector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Only created once, need to be thread-safe.
 */
public class ThriftServiceDetector implements RpcInvokerDetector {

    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) throws Exception {
        // TODO: use service discovery.
        // temporary hard code here.
        SocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 9090);
        return new ThriftNonblockingInvoker(invokerDef, remoteAddress);
    }
}
