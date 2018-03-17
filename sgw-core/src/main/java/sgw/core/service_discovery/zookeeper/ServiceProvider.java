package sgw.core.service_discovery.zookeeper;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.load_balancer.DynamicLoadBalancer;
import sgw.core.load_balancer.RoundRobinDynamicLoadBalancer;
import sgw.core.service_channel.RpcType;
import sgw.core.service_discovery.ServiceNode;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServiceProvider {

    private final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private String serviceName; // registered service name
    private String ZKPrefixPath;
    private CuratorFramework client;
    private TreeCache ZKCache;
    private final DynamicLoadBalancer<ServiceNode> loadBalancer;

    public ServiceProvider(String serviceRegName, CuratorFramework ZkClient) {
        serviceName = serviceRegName;
        client = ZkClient;
        ZKPrefixPath = prefixPath();
        // use RoundRobinDynamicLoadBalancer as default
        loadBalancer = new RoundRobinDynamicLoadBalancer<>();
    }

    // subclasses may override this method to customize zookeeper path.
    protected String prefixPath() {
        return "/" + serviceName + "/servers";
    }

    public void startListening() throws Exception {
        ZKCache = new TreeCache(client, ZKPrefixPath);
        ZKCache.getListenable().addListener(
            (client, event) -> {
                switch (event.getType()) {
                    case NODE_ADDED:
                        nodeAddedCallback(event, loadBalancer);
                        break;
                    case NODE_REMOVED:
                        nodeRemovedCallBack(event, loadBalancer);
                        break;
                }
            });
        ZKCache.start();
    }

    private void nodeAddedCallback(TreeCacheEvent event, DynamicLoadBalancer<ServiceNode> lb) throws Exception {
        ChildData childData = event.getData();
        String path = childData.getPath();
        if (path.length() > ZKPrefixPath.length()) {
            // new instance added
            String nodeName = path.substring(ZKPrefixPath.length());
            ServiceNode newNode = new ServiceNodeImpl(serviceName, nodeName);
            newNode.loadNodeData(childData.getData());
            lb.add(newNode);
            logger.info("New service node added to service: \"{}\".", serviceName);
        }
    }

    private void nodeRemovedCallBack(TreeCacheEvent event, DynamicLoadBalancer<ServiceNode> lb) throws Exception {
        ChildData childData = event.getData();
        String path = childData.getPath();
        if (path.length() > ZKPrefixPath.length()) {
            // instance removed
            String nodeName = path.substring(ZKPrefixPath.length());
            ServiceNode node = new ServiceNodeImpl(serviceName, nodeName);
            lb.remove(node);
            logger.info("Service node removed from service: \"{}\", {} left.", serviceName, lb.size());
        }
    }

    public void stopListening() {
        ZKCache.close();
    }

    public ServiceNode next() {
        return loadBalancer.next();
    }

    public static class ServiceNodeImpl implements ServiceNode {

        private String serviceName; // service name in zookeeper
        private String name; // true name = serviceName + '@' + nodeName;
        private RpcType protocol;

        private String ip;
        private int port;

        public ServiceNodeImpl(String serviceName, String nodeName) {
            this.serviceName = serviceName;
            this.name = serviceName + '@' + nodeName;
        }

        @Override
        public SocketAddress remoteAddress() {
            return new InetSocketAddress(ip, port);
        }

        @Override
        public String serviceName() {
            return serviceName;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        public RpcType protocol() { return protocol; }

        @Override
        public void loadNodeData(byte[] data) throws Exception {
            String st = new String(data, "utf8");
            JSONObject obj = JSONObject.parseObject(st);
            ip = obj.getString("ip");
            port = obj.getInteger("port");
            protocol = RpcType.valueOf(obj.getString("protocol"));
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServiceNodeImpl))
                return false;
            ServiceNodeImpl other = (ServiceNodeImpl) o;
            return other.name.equals(name) && other.ip.equals(ip) && other.port == port;
        }
    }

}
