package sgw.core.services;

import sgw.NettyGatewayServerConfig;
import sgw.core.services.thrift.ThriftServiceDetector;

public class RpcInvokerDetectorFactory {

    public RpcInvokerDetectorFactory(NettyGatewayServerConfig config) {

    }

    public RpcInvokerDetector create() {
        // TODO: create serviceDetector according to config.
        return new ThriftServiceDetector();
    }
}
