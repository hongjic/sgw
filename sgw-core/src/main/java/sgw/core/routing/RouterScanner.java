package sgw.core.routing;

import io.netty.handler.codec.http.HttpMethod;
import sgw.core.data_convertor.Convertors;
import sgw.core.data_convertor.annotations.ThriftRouter;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;
import sgw.core.service_channel.thrift.ThriftInvokerDef;
import sgw.core.util.PackageScanner;

import java.lang.annotation.Annotation;
import java.util.*;

public class RouterScanner extends PackageScanner implements RouterInitializer {

    private static final Class<sgw.core.data_convertor.annotations.Router> BASIC = sgw.core.data_convertor.annotations.Router.class;
    private static final Class<ThriftRouter> THRIFT = ThriftRouter.class;

    @Override
    protected boolean isWanted(Class<?> clazz) {
        if (clazz.isAnnotationPresent(BASIC))
            return true;
        Annotation[] ans = clazz.getDeclaredAnnotations();
        for (Annotation an: ans)
            if (an.annotationType().isAnnotationPresent(BASIC))
                return true;
        return false;
    }

    public RouterScanner ofPackage(String packageName) {
        return (RouterScanner) super.ofPackage(packageName);
    }

    public RouterScanner ofPackage(String packageName, boolean recursive) {
        return (RouterScanner) super.ofPackage(packageName, recursive);
    }

    /**
     * Initilaize router and load convertors into cache.
     * @return
     */
    @Override
    public Router init() throws Exception {
        scan();
        Map<HttpRequestDef, RpcInvokerDef> map = new HashMap<>();
        Collection<Class<?>> clazzs = new ArrayList<>();
        // init all services
        initThrift(map, clazzs);

        // cache convertors
        Convertors.Cache.cacheAllConvertors(clazzs);

        // init router
        Router router = new Router();
        router.initialize(map);
        return router;
    }

    private void initThrift(final Map<HttpRequestDef, RpcInvokerDef> map, final Collection<Class<?>> clazzs) {
        Iterator<Class<?>> iter = allClasses.iterator();
        ThriftRouter tr;
        while (iter.hasNext()) {
            Class<?> convertorClazz = iter.next();
            if ((tr = convertorClazz.getAnnotation(THRIFT)) != null) {
                HttpRequestDef reqDef = new HttpRequestDef(
                        HttpMethod.valueOf(tr.http()[0]),
                        tr.http()[1]);

                RpcInvokerDef invokerDef = new ThriftInvokerDef(
                        RpcType.Thrift,
                        tr.service(),
                        tr.method(),
                        tr.args(),
                        tr.result(),
                        convertorClazz.getName());
                map.put(reqDef, invokerDef);
                clazzs.add(convertorClazz);
            }
        }
    }

}
