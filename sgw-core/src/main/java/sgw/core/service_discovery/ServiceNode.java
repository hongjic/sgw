package sgw.core.service_discovery;

import sgw.core.service_channel.RpcType;

import java.net.SocketAddress;

public interface ServiceNode {

    SocketAddress remoteAddress();

    String serviceName();

    String nodeName();

    RpcType protocol();

    /**
     * parse and load zookeeper node data
     * @param data zookeeper node data
     * @throws Exception
     */
    void loadNodeData(byte[] data) throws Exception;
}
