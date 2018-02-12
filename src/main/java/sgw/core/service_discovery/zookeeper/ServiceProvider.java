package sgw.core.service_discovery.zookeeper;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import sgw.core.service_discovery.LoadBalancer;
import sgw.core.service_discovery.RoundRobinLoadBalancer;
import sun.reflect.annotation.ExceptionProxy;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServiceProvider {

    private String serviceName; // registered service name
    private String ZKPrefixPath;
    private CuratorFramework client;
    private TreeCache ZKCache;
    private final RoundRobinLoadBalancer<ServiceNode> loadBalancer;

    public ServiceProvider(String serviceRegName, CuratorFramework ZkClient) {
        serviceName = serviceRegName;
        ZKPrefixPath = "/" + serviceName + "/servers";
        client = ZkClient;
        loadBalancer = new RoundRobinLoadBalancer<>();
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
                System.out.println(event.getType());
                if (event.getData() != null) {
                    System.out.println(event.getData().getPath());
                    System.out.println(new String(event.getData().getData(), "utf8"));
                }
                System.out.println();
            });
        ZKCache.start();
    }

    private void nodeAddedCallback(TreeCacheEvent event, LoadBalancer<ServiceNode> lb) throws Exception {
        ChildData childData = event.getData();
        String path = childData.getPath();
        if (path.length() > ZKPrefixPath.length()) {
            // new instance added
            String nodeName = path.substring(ZKPrefixPath.length());
            ServiceNode newNode = new ServiceNodeImpl(nodeName);
            newNode.loadNodeData(childData.getData());
            lb.add(newNode);
        }
    }

    private void nodeRemovedCallBack(TreeCacheEvent event, LoadBalancer<ServiceNode> lb) throws Exception {
        ChildData childData = event.getData();
        String path = childData.getPath();
        if (path.length() > ZKPrefixPath.length()) {
            // instance removed
            String nodeName = path.substring(ZKPrefixPath.length());
            ServiceNode node = new ServiceNodeImpl(nodeName);
            lb.remove(node);
        }
    }

    public void stopListening() {
        ZKCache.close();
    }

    public ServiceNode next() {
        return loadBalancer.next();
    }

    public static class ServiceNodeImpl implements ServiceNode {

        /**
         * Node name should be unique inside a serivce.
         * Correctness of `hashCode()` and `equals()` depend on this.
         */
        private String nodeName;

        private String ip;
        private int port;

        public ServiceNodeImpl(String nodeName) {
            this.nodeName = nodeName;
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress(ip, port);
        }

        @Override
        public void loadNodeData(byte[] data) throws Exception {
            String st = new String(data, "utf8");
            JSONObject obj = JSONObject.parseObject(st);
            ip = obj.getString("ip");
            port = obj.getInteger("port");
        }

        @Override
        public int hashCode() {
            return nodeName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ServiceNodeImpl))
                return false;
            ServiceNodeImpl other = (ServiceNodeImpl) o;
            return other.nodeName.equals(nodeName);
        }
    }

}
