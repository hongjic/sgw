import io.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import sgw.core.HttpRequestDef;
import sgw.core.routing.*;
import sgw.core.services.RpcInvokerDef;

import static org.junit.Assert.assertEquals;

public class RouterPropertiesFileCompilerTest {

    String filepath = "src/test/resources/routing.properties";

    @Test
    public void testCompile() {
        RouterDataSource source = new RouterDataSource(RouterDataSource.Type.PROPERTIES_FILE);
        source.setPropertiesFilePath(filepath);
        RouterGeneratorFactory factory = new RouterGeneratorFactory(source);
        RouterGenerator compiler = factory.create();
        Router router = null;
        try {
            router = compiler.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpRequestDef reqDef = new HttpRequestDef(HttpMethod.GET, "/test1");
        RpcInvokerDef invokerDef = router.getRpcInvokerDef(reqDef);

        assertEquals(invokerDef.getServiceName(), "demoservice");
        assertEquals(invokerDef.getMethodName(), "test");
        assertEquals(invokerDef.getParamConvertor(), "HttpBodyToString");
        assertEquals(invokerDef.getResultConvertor(), "StringToHttpResponse");
    }
}
