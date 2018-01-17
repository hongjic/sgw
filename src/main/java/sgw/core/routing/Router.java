package sgw.core.routing;

import sgw.core.HttpRequestDef;
import sgw.core.services.RpcInvokerDef;

import java.util.HashMap;

/**
 * Shared among threads, need to be thread-safe after `initialized` becomes ture.
 */
public class Router {

    private static Router router;

    private HashMap<HttpRequestDef, RpcInvokerDef> mapping;
    private boolean initialized;
    
    public Router() {
        mapping = new HashMap<>();
        initialized = false;
    }

    public RpcInvokerDef getRpcInvokerDef(HttpRequestDef reqDef) {
        return mapping.get(reqDef);
    }

    public RpcInvokerDef putRouting(HttpRequestDef reqDef, RpcInvokerDef invokerDef) {
        return mapping.put(reqDef, invokerDef);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialized() {
        initialized = true;
    }

}
