package sgw.core.http_channel.routing;

import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;

import java.util.HashMap;

/**
 * Shared among threads, need to be thread-safe after `initialized` becomes ture.
 */
public class Router {

    private HashMap<HttpRequestDef, RpcInvokerDef> invokerDefMapping;
    private HashMap<HttpRequestDef, FullHttpRequestParser> reqParserMapping;
    private HashMap<HttpRequestDef, FullHttpResponseGenerator> resGeneratorMapping;
    private boolean initialized;
    
    public Router() {
        invokerDefMapping = new HashMap<>();
        reqParserMapping = new HashMap<>();
        resGeneratorMapping = new HashMap<>();
        initialized = false;
    }

    public RpcInvokerDef getRpcInvokerDef(HttpRequestDef reqDef) {
        return invokerDefMapping.get(reqDef);
    }

    public FullHttpRequestParser getRequestParser(HttpRequestDef reqDef) {
        return reqParserMapping.get(reqDef);
    }

    public FullHttpResponseGenerator getResponseGenerator(HttpRequestDef reqDef) {
        return resGeneratorMapping.get(reqDef);
    }

    public void putRouting(HttpRequestDef reqDef,
                                    RpcInvokerDef invokerDef,
                                    FullHttpRequestParser requestParser,
                                    FullHttpResponseGenerator resGenerator) {
        invokerDefMapping.put(reqDef, invokerDef);
        reqParserMapping.put(reqDef, requestParser);
        resGeneratorMapping.put(reqDef, resGenerator);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialized() {
        initialized = true;
    }

}
