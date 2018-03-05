import io.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.routing.Router;
import sgw.core.routing.UndefinedHttpRequestException;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RouterTest {

    String configFilePath = "src/test/resources/routing.yaml";

    Map<HttpRequestDef, RpcInvokerDef> map;
    HttpRequestDef reqDef1 = new HttpRequestDef(HttpMethod.POST, "/aaa");
    HttpRequestDef reqDef2 = new HttpRequestDef(HttpMethod.POST, "/bbb");
    HttpRequestDef reqDef3 = new HttpRequestDef(HttpMethod.GET, "/aaa/{id:[0-9]+}");
    HttpRequestDef reqDef3_1;
    HttpRequestDef reqDef4 = new HttpRequestDef(HttpMethod.DELETE, "/aaa/{id:[0-9]+}");
    HttpRequestDef reqDef4_1;
    HttpRequestDef reqDef5 = new HttpRequestDef(HttpMethod.GET, "/aaa/*");
    HttpRequestDef reqDef5_1;
    Router router;

    void initialize() {
        reqDef3_1 = new HttpRequestDef(HttpMethod.GET, "/aaa/3");
        reqDef4_1 = new HttpRequestDef(HttpMethod.DELETE, "/aaa/3");
        reqDef5_1 = new HttpRequestDef(HttpMethod.GET, "/aaa/ccc");
        router = new Router();
        router.initialize(map);
    }

    public RouterTest() {
        map = new HashMap<>();
        map.put(reqDef1, new RpcInvokerDef(
                RpcType.Thrift,
                "service1", "method1",
                null));
        map.put(reqDef2, new RpcInvokerDef(
                RpcType.Thrift,
                "service2", "method1",
                null));
        map.put(reqDef3, new RpcInvokerDef(
                RpcType.Thrift,
                "service1", "method2",
                null));
        map.put(reqDef4, new RpcInvokerDef(
                RpcType.Thrift,
                "service1", "method3",
                null));
    }


    @Test
    public void testInitialize() {
        initialize();
        try {
            RpcInvokerDef invokerDef;
            invokerDef = router.get(reqDef1);
            checkInvokerDef("service1", "method1", invokerDef);

            invokerDef = router.get(reqDef2);
            checkInvokerDef("service2", "method1", invokerDef);

            invokerDef = router.get(reqDef3_1);
            checkInvokerDef("service1", "method2", invokerDef);

            invokerDef = router.get(reqDef4_1);
            checkInvokerDef("service1", "method3", invokerDef);
        } catch (UndefinedHttpRequestException e) {
            fail();
        }
    }

    @Test
    public void testGet() {
        initialize();
        RpcInvokerDef invokerDef;
        try {
            invokerDef = router.get(reqDef3_1);
            Map<String, Object> param = new HashMap<>();
            param.put("id", "3");
            checkInvokerDef("service1", "method2", invokerDef);
            assertEquals(param, reqDef3_1.getParams());
        } catch (UndefinedHttpRequestException e) {
            fail();
        }

        assertUndefinedRequest(reqDef5_1);

        // overrride
        router.put(reqDef5, new RpcInvokerDef(
                RpcType.Thrift,
                "service1", "method4",
                null));
        try {
            invokerDef = router.get(reqDef5_1);
            checkInvokerDef("service1", "method4", invokerDef);
        } catch (UndefinedHttpRequestException e) {
            fail();
        }
    }

    @Test
    public void testPut() {
        initialize();
        try {
            RpcInvokerDef invokerDef;
            invokerDef = router.put(reqDef1, new RpcInvokerDef(
                    RpcType.Thrift,
                    "service9", "method9",
                    null));
            checkInvokerDef("service1", "method1", invokerDef);

            invokerDef = router.get(reqDef1);
            checkInvokerDef("service9", "method9", invokerDef);
        } catch (UndefinedHttpRequestException e) {
            fail();
        }
    }

    @Test
    public void testPutAll() {
        initialize();
        try {
            Map<HttpRequestDef, RpcInvokerDef> m = new HashMap<>();
            m.put(reqDef1, new RpcInvokerDef(
                    RpcType.Thrift,
                    "service9", "method9",
                    null));
            m.put(reqDef2, new RpcInvokerDef(
                    RpcType.Thrift,
                    "service2", "method2",
                    null));
            router.putAll(m);

            RpcInvokerDef invokerDef;
            invokerDef = router.get(reqDef1);
            checkInvokerDef("service9", "method9", invokerDef);

            invokerDef = router.get(reqDef2);
            checkInvokerDef("service2", "method2", invokerDef);
        } catch (UndefinedHttpRequestException e) {
            fail();
        }
    }

    @Test
    public void testRemove() {
        initialize();

        RpcInvokerDef invokerDef;
        invokerDef = router.remove(reqDef1);
        checkInvokerDef("service1", "method1", invokerDef);

        assertUndefinedRequest(reqDef1);
    }

    @Test
    public void testRemoveAll() {
        initialize();

        router.removeAll(Arrays.asList(reqDef1, reqDef5));

        assertUndefinedRequest(reqDef1);
        assertUndefinedRequest(reqDef5_1);
    }

    @Test
    public void testClear() {
        initialize();

        router.clear();
        assertUndefinedRequest(reqDef1);
        assertUndefinedRequest(reqDef2);
        assertUndefinedRequest(reqDef3_1);
        assertUndefinedRequest(reqDef4_1);
        assertUndefinedRequest(reqDef5_1);
    }

    @Test
    public void testLoadFromConfig() {
        try {
            router = Router.createFromConfig(configFilePath);
            RpcInvokerDef invokerDef;
            invokerDef = router.get(new HttpRequestDef(HttpMethod.POST, "/echo"));
            checkInvokerDef("echoservice", "echo", invokerDef);
            invokerDef = router.get(new HttpRequestDef(HttpMethod.POST, "/echoplus"));
            checkInvokerDef("echoplusservice", "echo", invokerDef);
        } catch (Exception e) {
            fail();
        }

    }

    private void checkInvokerDef(String serviceName, String methodName, RpcInvokerDef def) {
        assertEquals(serviceName, def.getServiceName());
        assertEquals(methodName, def.getMethodName());
    }

    private void assertUndefinedRequest(HttpRequestDef def) {
        boolean exception = false;
        try {
            router.get(def);
        } catch (UndefinedHttpRequestException e) {
            exception = true;
        }
        assertTrue(exception);
    }


}
