package sgw.core.service_discovery;

import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.zookeeper.ZKServiceDiscoverer;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An interface for the basic functionality of service discovery.
 * Enable different implementations. All implementations should be
 * thread-safe.
 */
public interface RpcInvokerDiscoverer {

    List<String> getAllServices();

    /**
     * Always call connect before find.
     * @return a ChannelFuture representing the connect request
     */
    ChannelFuture connectAsync(RpcInvokerDef invokerDef) throws Exception;

    /**
     * @return a ChannelFuture representing the find request.
     */
    ChannelFuture findAsync(RpcInvokerDef invokerDef) throws Exception;

    RpcInvoker find(RpcInvokerDef invokerDef) throws Exception;

    enum Impl {
        Zookeeper
    }

    class Builder {

        private final Logger logger = LoggerFactory.getLogger(Builder.class);
        private static final String CONFIG_PATH = "src/main/resources/discovery.properties";
        private static final String IMPL_FIELD = "impl";
        private static final String SERVICE_FIELD = "service_names";

        private Impl impl; // e.g. Impl.Zookeeper
        private List<String> serviceNames;

        public Builder() {
            serviceNames = new ArrayList<>();
        }

        public Builder withImpl(Impl impl) {
            this.impl = impl;
            return this;
        }

        public Builder withServices(String... services) {
            for (String service: services)
                serviceNames.add(service);
            return this;
        }

        public Builder loadFromConfig() throws Exception {
            return loadFromConfig(CONFIG_PATH);
        }

        public Builder loadFromConfig(String configPath) throws Exception {
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

            withImpl(Impl.valueOf(prop.getProperty(IMPL_FIELD)));
            withServices(prop.getProperty(SERVICE_FIELD).split(","));
            return this;
        }

        public RpcInvokerDiscoverer build() {
            switch (impl) {
                case Zookeeper:
                    return new ZKServiceDiscoverer(serviceNames);
                default:
                    return null;
            }
        }
    }
}
