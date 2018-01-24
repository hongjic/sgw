package sgw.core.data_convertor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will be shared among threads.
 * But usually there will only be concurrent reads but not writes.
 * All the write ops are completed before the server starts receiving requests.
 */
public enum Convertors {

    Cache;

    private final Logger logger = LoggerFactory.getLogger(Convertors.class);

    private ConcurrentHashMap<String, FullHttpRequestParser> reqParCache;
    private ConcurrentHashMap<String, FullHttpResponseGenerator> resGenCache;

    Convertors() {
        reqParCache = new ConcurrentHashMap<>();
        resGenCache = new ConcurrentHashMap<>();
    }

    public FullHttpRequestParser getReqParser(String clazzName) throws IllegalArgumentException {
        if (!reqParCache.containsKey(clazzName))
            throw new IllegalArgumentException("Http Request Parser named '" +
            clazzName + "' is not found in cache.");
        return reqParCache.get(clazzName);
    }

    public FullHttpResponseGenerator getResGen(String clazzName) throws IllegalArgumentException {
        if (!resGenCache.containsKey(clazzName))
            throw new IllegalArgumentException("Http Response Generator named '" +
            clazzName + "' is not found is cache.");
        return resGenCache.get(clazzName);
    }

    // should not be called after server starts
    // **idempotent**
    public FullHttpRequestParser createReqParser(String clazzName) throws Exception {
        if (reqParCache.containsKey(clazzName))
            return reqParCache.get(clazzName);

        FullHttpRequestParser parser;
        try {
            Class clazz = Class.forName(clazzName);
            parser = (FullHttpRequestParser) clazz.newInstance();
            reqParCache.put(clazzName, parser);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot find data convertor named as {}", clazzName);
            throw e;
        }
        return parser;
    }

    // should not be called after server starts
    // **idempotent**
    public FullHttpResponseGenerator createResGen(String clazzName) throws Exception {
        if (resGenCache.containsKey(clazzName))
            return resGenCache.get(clazzName);

        FullHttpResponseGenerator generator;
        try {
            Class clazz = Class.forName(clazzName);
            generator = (FullHttpResponseGenerator) clazz.newInstance();
            resGenCache.put(clazzName, generator);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot find data convertor named as {}", clazzName);
            throw e;
        }
        return generator;
    }
}
