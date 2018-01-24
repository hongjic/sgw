package sgw.core.service_channel;

import sgw.NettyGatewayServerConfig;
import sgw.core.service_channel.zookeeper.ZKServiceDetector;

public class RpcInvokerDetectorFactory {

    private static final String ZOOKEEPER = "zookeeper";

    private String impl;

    public RpcInvokerDetectorFactory(NettyGatewayServerConfig config) {
        this.impl = config.getServiceDiscoveryImpl();
    }

    public RpcInvokerDetector create() {
        switch (impl) {
            case ZOOKEEPER:
                return new ZKServiceDetector();
            default:
                return null;
        }
    }
}
