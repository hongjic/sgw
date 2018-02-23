package sgw.core.http_channel.routing;

import org.yaml.snakeyaml.Yaml;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.thrift.ThriftInvokerDef;
import sgw.core.util.CopyOnWriteHashMap;

import java.util.*;

/**
 * thread safe. see {@link CopyOnWriteHashMap} for performance detail.
 */
public class Router {

    private CopyOnWriteHashMap<HttpRequestDef, RpcInvokerDef> map;

    public Router() {
        map = new CopyOnWriteHashMap<>();
    }

    /**
     *
     * @param reqDef http request definition
     * @return corresponding rpc request definition
     */
    public RpcInvokerDef get(HttpRequestDef reqDef) throws UndefinedHttpRequestException {
        if (map.containsKey(reqDef))
            return map.get(reqDef);
        else
            throw new UndefinedHttpRequestException(reqDef);
    }

    /**
     *
     * @return String representing the YAML format content of the current routing setting.
     */
    public String generateYaml() {
        // TODO: return yaml file content.
        // generate a Routing
        final Set<Map.Entry<HttpRequestDef, RpcInvokerDef>> entrySet = map.entrySet();
        List<YamlRouterCompiler.ThriftAPI> list = new ArrayList<>();
        for (Map.Entry<HttpRequestDef, RpcInvokerDef> entry: entrySet) {
            YamlRouterCompiler.ThriftAPI api = new YamlRouterCompiler.ThriftAPI();
            HttpRequestDef httpDef = entry.getKey();
            ThriftInvokerDef thriftDef = (ThriftInvokerDef) entry.getValue();
            api.setClazz(thriftDef.getThriftClazz());
            api.setHttp(httpDef.getHttpMethod().name() + " " + httpDef.getUri());
            api.setMethod(thriftDef.getMethodName());
            api.setRequestParser(thriftDef.getRequestParser());
            api.setResponseGenerator(thriftDef.getResponseGenerator());
            api.setService(thriftDef.getServiceName());
            list.add(api);
        }
        YamlRouterCompiler.RoutingData data = new YamlRouterCompiler.RoutingData();
        data.setThriftServices(list);

        return new Yaml().dump(data);
    }

    /** Modify single mapping. Don't use this method during initialization, use {@link #clear()} instead.
     *
     * @param reqDef http request definition
     * @param invokerDef rpc request definition
     * @return the previous defined rpc request, null if no previous
     */
    public RpcInvokerDef put(HttpRequestDef reqDef, RpcInvokerDef invokerDef) {
        return map.put(reqDef, invokerDef);
    }

    /**
     *
     * @param reqDef http request definition
     * @return the removed rpc request definition, null if no previous
     */
    public RpcInvokerDef remove(HttpRequestDef reqDef) {
        return map.remove(reqDef);
    }

    /**
     * clear all routing setting.
     */
    public void clear() {
        map.clear();
    }

    /**
     * clear all and load. e.g. initialization
     * @param hashmap http request --> rpc request mapping
     */
    public void clearAndLoad(HashMap<HttpRequestDef, RpcInvokerDef> hashmap) {
        map.clearAndPutAll(hashmap);
    }

    public static Router createFromConfig() throws Exception {
        // first try Yaml
        RouterCompiler compiler;
        if ((compiler = new YamlRouterCompiler()).checkExist()) {
            return compiler.compile();
        }
//        else if ((compiler = new PropertiesRouterCompiler()).checkExist()) {
//            return compiler.compile();
//        }
        else {
            // return empty router
            return new Router();
        }
    }

}
