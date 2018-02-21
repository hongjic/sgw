package sgw.core.http_channel.routing;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.Convertors;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class RouterPropertiesFileCompiler implements RouterGenerator{

    private static final int NUM_KV = 5;
    private static final int PROTOCOL = 0;
    private static final int METHOD = 1;
    private static final int URI = 2;
    private static final int PARAM_CONVERTOR = 3;
    private static final int RESULT_CONVERTOR = 4;

    private static final class Tuple {
        final String a, b;
        int h;

        Tuple(String x, String y) {
            a = x; b = y;
        }

        @Override
        public int hashCode() {
            h = a.hashCode() * 31 + b.hashCode();
            return h;
        }

        @Override
        public boolean equals(Object o){
            if (!(o instanceof Tuple))
                return false;
            Tuple other = (Tuple) o;
            return a.equals(other.a) && b.equals(other.b);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(RouterPropertiesFileCompiler.class);

    private File routerFile;
    private HashMap<Tuple, Tuple[]> hashMap;

    public RouterPropertiesFileCompiler(File file) {
        routerFile = file;
        hashMap = new HashMap<>();
    }

    @Override
    public Router generate() throws Exception {
        logger.info("Start generating router.");

        Router router = new Router();
        Properties prop;
        try {
            FileInputStream fileInput = new FileInputStream(routerFile);
            prop = new Properties();
            prop.load(fileInput);
            fileInput.close();
        } catch (Exception e) {
            logger.error("Failed generating router: {}", e.getMessage());
            throw e;
        }

        Enumeration keys = prop.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = prop.getProperty(key);
            addRouting(router, key, value);
        }

        // check if something is missing in the config file.
        if (!hashMap.isEmpty()) {
            logger.error("Routing configuration wrong.");
            hashMap.clear();
            throw new IllegalStateException("Routing configuration wrong.");
        }
        logger.info("Finished generating router.");
        router.initialized();

        return router;
    }

    private void addRouting(Router router, String key, String value) throws Exception {
        String[] blocks = key.split("\\.");
        String serviceName = blocks[0];
        String methodName = blocks[1];
        String k = blocks[2];
        Tuple t1 = new Tuple(serviceName, methodName);
        Tuple t2 = new Tuple(k, value);

        if (!hashMap.containsKey(t1))
            hashMap.put(t1, new Tuple[NUM_KV]);
        Tuple[] pairs = hashMap.get(t1);

        if (k.equals("protocol")) {
            pairs[PROTOCOL] = t2;
        }
        else if (k.equals("method")) {
            pairs[METHOD] = t2;
        }
        else if (k.equals("uri")) {
            pairs[URI] = t2;
        }
        else if (k.equals("param_convertor")) {
            pairs[PARAM_CONVERTOR] = t2;
        }
        else if (k.equals("result_convertor")) {
            pairs[RESULT_CONVERTOR] = t2;
        }

        // if the current `pairs` is full, convert to HttpRequestDef and RpcInvokerDef
        if (oneRouteFinished(pairs)) {
            HttpMethod method = HttpMethod.valueOf(pairs[METHOD].b);
            HttpRequestDef reqDef = new HttpRequestDef(method, pairs[URI].b);

            RpcInvokerDef def;
            FullHttpRequestParser reqPar;
            FullHttpResponseGenerator resGen;
            try {
                RpcType protocol = RpcType.valueOf(pairs[PROTOCOL].b);
                reqPar = Convertors.Cache.createReqParser(pairs[PARAM_CONVERTOR].b);
                resGen = Convertors.Cache.createResGen(pairs[RESULT_CONVERTOR].b);
                def = new RpcInvokerDef(serviceName, methodName, protocol);
            } catch (IllegalArgumentException e) {
                // this is for RpcType.valueOf
                logger.error("Invalid protocol for {}.{}", serviceName, methodName);
                throw e;
            }
            hashMap.remove(t1);
            router.putRouting(reqDef, def, reqPar, resGen);
        }
    }

    private boolean oneRouteFinished(Tuple[] pairs) {
        for (int i = 0; i < NUM_KV; i ++)
            if (pairs[i] == null)
                return false;
        return true;
    }

}
