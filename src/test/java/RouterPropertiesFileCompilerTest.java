import io.netty.handler.codec.http.HttpMethod;
import org.junit.Test;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.http_channel.routing.*;
import sgw.core.service_channel.RpcInvokerDef;

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
        HttpRequestDef reqDef = new HttpRequestDef(HttpMethod.POST, "/aaa");
        RpcInvokerDef invokerDef = router.getRpcInvokerDef(reqDef);
        assertEquals(invokerDef.getServiceName(), "EchoService");
        assertEquals(invokerDef.getMethodName(), "echo");

        FullHttpRequestParser reqPar = router.getRequestParser(reqDef);
        assertEquals(reqPar.getClass().getName(), "sgw.parser.EchoServiceEchoParams");

        FullHttpResponseGenerator resGen = router.getResponseGenerator(reqDef);
        assertEquals(resGen.getClass().getName(), "sgw.parser.EchoServiceEchoResult");
    }
}
