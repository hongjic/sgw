package sgw.core.service_discovery.zookeeper;

import java.net.SocketAddress;

public interface ServiceNode {

    SocketAddress getRemoteAddress();

    void loadNodeData(byte[] data) throws Exception;
}
