package sgw.core.data_convertor;

import sgw.core.util.CopyOnWriteHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will be shared among threads.
 * But usually there will only be concurrent reads but not writes.
 * All the write ops are completed before the server starts receiving requests.
 */
public enum Convertors {

    Cache;

    private CopyOnWriteHashMap<String, ConvertorInfo> infoCache;

    Convertors() {
        infoCache = new CopyOnWriteHashMap<>();
    }

    public ConvertorInfo getConvertorInfo(String httpConvertorClazzName) {
        if (!infoCache.containsKey(httpConvertorClazzName))
            throw new IllegalArgumentException("Http convertor class info named as {" +
                    httpConvertorClazzName + "} is not found in cache.");
        return infoCache.get(httpConvertorClazzName);
    }

    public void cacheAllConvertorsByName(Collection<String> col) throws Exception {
        Map<String, ConvertorInfo> map = new HashMap<>();
        for (String clazzName: col) {
            map.put(clazzName, ConvertorInfo.create(clazzName));
        }
        infoCache.putAll(map);
    }

    public void cacheAllConvertors(Collection<Class<?>> col) throws Exception {
        Map<String, ConvertorInfo> map = new HashMap<>();
        for (Class<?> clazz: col) {
            map.put(clazz.getName(), ConvertorInfo.create(clazz));
        }
        infoCache.putAll(map);
    }
}
