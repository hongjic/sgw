package sgw.core.service_discovery.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.RpcType;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.service_channel.thrift.ThriftNonblockingInvoker;
import sgw.core.service_discovery.ServiceUnavailableException;

import java.io.FileInputStream;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Only created once, need to be thread-safe.
 */
public class ZKServiceDiscoverer implements RpcInvokerDiscoverer {

    private List<String> serviceNames;
    private HashMap<String, ServiceProvider> serviceProviderMap; // key is className
    private CuratorFramework ZkClient;
    private boolean started;

    public ZKServiceDiscoverer(List<String> serviceNames, CuratorFramework curatorClient) {
        this.serviceNames = serviceNames;
        serviceProviderMap = new HashMap<>();
        ZkClient = curatorClient;
        started = false;
    }

    public void start() throws Exception {
        ZkClient.start();
        for (String serviceName: serviceNames) {
            ServiceProvider sp = new ServiceProvider(serviceName, ZkClient);
            // listen to changes and auto update
            sp.startListening();
            serviceProviderMap.put(serviceName, sp);
        }
        started = true;
    }

    public void close() {
        for (ServiceProvider sp: serviceProviderMap.values()) {
            sp.stopListening();
        }
        ZkClient.close();
        started = false;
    }

    @Override
    public List<String> getAllServices() {
        return serviceNames;
    }

    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) throws ServiceUnavailableException {
        if (!started)
            throw new IllegalStateException("Discoverer is not started.");
        RpcType rpcProtocol = invokerDef.getProtocol();

        switch (rpcProtocol) {
            case Thrift: {
                String serviceName = invokerDef.getServiceName();
                ServiceProvider provider = serviceProviderMap.get(serviceName);
                ServiceNode node = provider.next();
                if (node != null) {
                    SocketAddress remoteAddress = node.getRemoteAddress();
                    return new ThriftNonblockingInvoker(invokerDef, remoteAddress);
                }
                else {
                    throw new ServiceUnavailableException(serviceName);
                }

            }
            default: return null;
        }
    }

    public static class Builder {

        private final Logger logger = LoggerFactory.getLogger(Builder.class);
        private static final String CONFIG_PATH = "src/main/resources/zookeeper.properties";

        private String connectString;
        private int sessionTimeOut;
        private int connectionTimeOut;
        private String namespace;
        private int baseSleepTime;
        private int maxRetry;

        public Builder loadFromConfig(String configPath) throws Exception {
            if (configPath == null)
                configPath = CONFIG_PATH;
            Properties prop;
            try {
                FileInputStream fileInput = new FileInputStream(configPath);
                prop = new Properties();
                prop.load(fileInput);
                fileInput.close();
            } catch (Exception e) {
                logger.error("Failed loading discovery configuration: {}", e.getMessage());
                throw e;
            }

            connectString = prop.getProperty("zookeeper.connectString");
            sessionTimeOut = Integer.valueOf(prop.getProperty("zookeeper.sessionTimeOut"));
            connectionTimeOut = Integer.valueOf(prop.getProperty("zookeeper.connectionTimeOut"));
            namespace = prop.getProperty("zookeeper.namespace");
            maxRetry = Integer.valueOf(prop.getProperty("zookeeper.maxRetry"));
            baseSleepTime = Integer.valueOf(prop.getProperty("zookeeper.baseSleepTimeMS"));
            return this;
        }

        public ZKServiceDiscoverer build(List<String> serviceNames) {
            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .sessionTimeoutMs(sessionTimeOut)
                    .connectionTimeoutMs(connectionTimeOut)
                    .namespace(namespace)
                    .retryPolicy(new ExponentialBackoffRetry(baseSleepTime, maxRetry))
                    .build();
            return new ZKServiceDiscoverer(serviceNames, client);
        }

    }

//    public static void main(String[] args) {
//        Builder builder = new Builder();
//        try {
//            ZKServiceDiscoverer discoverer = builder.loadFromConfig().build(Arrays.asList("EchoService"));
//            discoverer.start();
//            Thread.sleep(100000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
