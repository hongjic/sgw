package sgw.core.service_discovery.zookeeper;

import java.net.SocketAddress;

public interface ServiceNode {

    SocketAddress getRemoteAddress();

    /**
     * parse and load zookeeper node data
     * @param data zookeeper node data
     * @throws Exception
     */
    void loadNodeData(byte[] data) throws Exception;
}
