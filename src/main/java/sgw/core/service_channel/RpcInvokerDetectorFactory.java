package sgw.core.service_channel;

import sgw.NettyGatewayServerConfig;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.service_channel.thrift.ThriftServiceDetector;

public class RpcInvokerDetectorFactory {

    public RpcInvokerDetectorFactory(NettyGatewayServerConfig config) {

    }

    public RpcInvokerDetector create() {
        // TODO: create serviceDetector according to config.
        return new ThriftServiceDetector();
    }
}
