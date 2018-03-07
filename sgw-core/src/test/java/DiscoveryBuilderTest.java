import org.junit.Test;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.service_discovery.zookeeper.ZKServiceDiscoverer;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiscoveryBuilderTest {

    String filepath = "src/test/resources/discovery.properties";
    String ZkFilePath = "src/test/resources/zookeeper.properties";


    @Test
    public void testBuilder() {
        RpcInvokerDiscoverer.Builder builder = new RpcInvokerDiscoverer.Builder();
        RpcInvokerDiscoverer discoverer = null;
        try {
            discoverer = builder.loadFromConfig(filepath)
                    .build(ZkFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(discoverer instanceof ZKServiceDiscoverer);
        List<String > services = discoverer.getAllServices();
        assertEquals(Arrays.asList("EchoService", "EchoPlusService"), services);
    }
}
