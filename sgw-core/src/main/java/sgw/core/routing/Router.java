package sgw.core.routing;

import io.netty.handler.codec.http.HttpMethod;
import org.apache.http.annotation.ThreadSafe;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.util.CopyOnWriteHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Only one {@link Router} instance is necessary in a gateway process.
 *
 */
@ThreadSafe
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
    private final Map<HttpMethod, UriMatcher<RpcInvokerDef>> mappings = new HashMap<>();

    public Router() {
        for (HttpMethod method: METHODS)
            mappings.put(method, new UriMatcherImpl<>());
    }

    /**
     *
     * @param reqDef http request definition
     * @return corresponding rpc request definition
     * @throws UndefinedHttpRequestException if request can not be found
     */
    public RpcInvokerDef get(HttpRequestDef reqDef) throws UndefinedHttpRequestException {
        UriMatcher<RpcInvokerDef> uriMatcher = uriMatcher(reqDef.getHttpMethod());
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
     * @return The {@link UriMatcher} found. Null if not found.
     */
    private UriMatcher<RpcInvokerDef> uriMatcher(HttpMethod method) {
        return mappings.get(method);
    }

    /** Modify single mapping. Don't use this method during initialization, use {@link #clear()} instead.
     *
     * @param reqDef http request definition
     * @param invokerDef rpc request definition
     * @return the previous defined rpc request, null if no previous
     */
    public RpcInvokerDef put(HttpRequestDef reqDef, RpcInvokerDef invokerDef) {
        return uriMatcher(reqDef.getHttpMethod()).register(reqDef.getUri(), invokerDef);
    }

    public void putAll(Map<HttpRequestDef, RpcInvokerDef> map) {
        for (HttpMethod method: METHODS) {
            Map<String, RpcInvokerDef> methodMapping = map
                    .entrySet().stream()
                    .filter(entry -> entry.getKey().getHttpMethod() == method)
                    .collect(Collectors.toMap(entry -> entry.getKey().getUri(), entry -> entry.getValue()));
            if (methodMapping.size() > 0)
                uriMatcher(method).registerAll(methodMapping);
        }
    }

    /**
     *
     * @param reqDef http request definition
     * @return the removed rpc request definition, null if no previous
     */
    public RpcInvokerDef remove(HttpRequestDef reqDef) {
        UriMatcher<RpcInvokerDef> uriMatcher = uriMatcher(reqDef.getHttpMethod());
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
                Optional.ofNullable(uriMatcher(method)).ifPresent(um -> um.unregisterAll(methodCol));
        }
    }

    /**
     * clear all routing setting.
     */
    public void clear() {
        for (UriMatcher matcher: mappings.values()) {
            matcher.clear();
        }
    }

    /**
     * clear all and load. e.g. initialization
     * @param map http request --> rpc request mapping
     */
    public void initialize(Map<HttpRequestDef, RpcInvokerDef> map) {
        clear();
        putAll(map);
    }

    public static Router initFromConfig() throws Exception {
        return initFromConfig(null);
    }

    public static Router initFromConfig(String filePath) throws Exception {
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
