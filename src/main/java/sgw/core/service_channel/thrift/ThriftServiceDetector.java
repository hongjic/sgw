package sgw.core.service_channel.thrift;

import io.netty.channel.ChannelFuture;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcInvokerDetector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Only created once, need to be thread-safe.
 */
public class ThriftServiceDetector implements RpcInvokerDetector {

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

    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) throws Exception {
        // temporary hard code here.
        SocketAddress remoteAddress = new InetSocketAddress(InetAddress.getLocalHost(), 9090);
        return new ThriftNonblockingInvoker(invokerDef, remoteAddress);
    }
}
