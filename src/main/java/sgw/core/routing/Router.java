package sgw.core.routing;

import sgw.core.HttpRequestDef;
import sgw.core.services.RpcInvokerDef;

import java.util.HashMap;

public class Router {

    private static Router router;

    private HashMap<HttpRequestDef, RpcInvokerDef> mapping;

    public Router() {
        mapping = new HashMap<>();
    }

    public RpcInvokerDef getRpcInvokerDef(HttpRequestDef reqDef) {
        return mapping.get(reqDef);
    }

    public RpcInvokerDef putRouting(HttpRequestDef reqDef, RpcInvokerDef invokerDef) {
        return mapping.put(reqDef, invokerDef);
    }

}
