package sgw.core.routing;

import io.netty.handler.codec.http.HttpMethod;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.util.CopyOnWriteHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * thread safe. see {@link CopyOnWriteHashMap} for performance detail.
 */
public class Router {

    private static final HttpMethod[] METHODS = new HttpMethod[] {
            HttpMethod.POST,
            HttpMethod.DELETE,
            HttpMethod.GET,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.HEAD,
            HttpMethod.OPTIONS,
            HttpMethod.TRACE
    };
    private final Map<HttpMethod, UriMatcher<RpcInvokerDef>> mappings;

    public Router() {
        mappings = new HashMap<>();
    }

    /**
     *
     * @param reqDef http request definition
     * @return corresponding rpc request definition
     * @throws UndefinedHttpRequestException if request can not be found
     */
    public RpcInvokerDef get(HttpRequestDef reqDef) throws UndefinedHttpRequestException {
        UriMatcher<RpcInvokerDef> uriMatcher = uriMatcher(reqDef.getHttpMethod(), false);
        if (uriMatcher == null)
            throw new UndefinedHttpRequestException(reqDef);

        String uri = reqDef.getUri();
        UriMatcher.UriMatchResult<RpcInvokerDef> matchResult = uriMatcher.lookup(uri);
        if (matchResult == null)
            throw new UndefinedHttpRequestException(reqDef);

        Map<String, String> params = matchResult.getParams();
        if (params != null && params.size() > 0) {
            reqDef.addParsedParams(params);
        }

        return matchResult.getObject();
    }

    /**
     *
     * @param method Http method
     * @param create Whether to create a new {@link UriMatcher} when {@param method} not found.
     * @return The {@link UriMatcher} found. Null if not found and {@param} create set to false.
     */
    private UriMatcher<RpcInvokerDef> uriMatcher(HttpMethod method, boolean create) {
        if (!create || mappings.containsKey(method))
            return mappings.get(method);

        UriMatcher<RpcInvokerDef> uriMatcher = new UriMatcherImpl<>();
        mappings.put(method, uriMatcher);
        return uriMatcher;
    }

    /** Modify single mapping. Don't use this method during initialization, use {@link #clear()} instead.
     *
     * @param reqDef http request definition
     * @param invokerDef rpc request definition
     * @return the previous defined rpc request, null if no previous
     */
    public RpcInvokerDef put(HttpRequestDef reqDef, RpcInvokerDef invokerDef) {
        return uriMatcher(reqDef.getHttpMethod(), true).register(reqDef.getUri(), invokerDef);
    }

    public void putAll(Map<HttpRequestDef, RpcInvokerDef> map) {
        for (HttpMethod method: METHODS) {
            Map<String, RpcInvokerDef> methodMapping = map
                    .entrySet().stream()
                    .filter(entry -> entry.getKey().getHttpMethod() == method)
                    .collect(Collectors.toMap(entry -> entry.getKey().getUri(), entry -> entry.getValue()));
            if (methodMapping.size() > 0)
                uriMatcher(method, true).registerAll(methodMapping);
        }
    }

    /**
     *
     * @param reqDef http request definition
     * @return the removed rpc request definition, null if no previous
     */
    public RpcInvokerDef remove(HttpRequestDef reqDef) {
        UriMatcher<RpcInvokerDef> uriMatcher = uriMatcher(reqDef.getHttpMethod(), false);
        if (uriMatcher == null)
            return null;

        return uriMatcher.unregister(reqDef.getUri());
    }

    public void removeAll(Collection<HttpRequestDef> col) {
        for (HttpMethod method: METHODS) {
            List<String> methodCol = col
                    .stream()
                    .filter(reqDef -> reqDef.getHttpMethod() == method)
                    .map(reqDef -> reqDef.getUri())
                    .collect(Collectors.toList());
            if (methodCol.size() > 0)
                Optional.ofNullable(uriMatcher(method, false)).ifPresent(um -> um.unregisterAll(methodCol));
        }
    }

    /**
     * clear all routing setting.
     */
    public void clear() {
        mappings.clear();
    }

    /**
     * clear all and load. e.g. initialization
     * @param map http request --> rpc request mapping
     */
    public void initialize(Map<HttpRequestDef, RpcInvokerDef> map) {
        mappings.clear();
        putAll(map);
    }

    public static Router createFromConfig() throws Exception {
        return createFromConfig(null);
    }

    public static Router createFromConfig(String filePath) throws Exception {
        // first try Yaml
        RouterCompiler compiler;
        if ((compiler = new YamlRouterCompiler(filePath)).checkExist()) {
            return compiler.compile();
        }
        else {
            // return empty router
            return new Router();
        }
    }

}
