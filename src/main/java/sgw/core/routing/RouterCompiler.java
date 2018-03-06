package sgw.core.routing;

import sgw.core.data_convertor.Convertors;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class RouterCompiler implements RouterInitializer {

    protected File configFile;

    /**
     * check if configuration file exists
     * @return true if exists
     */
    public boolean checkExist() {
        if (configFile == null)
            configFile = new File(getFilePath());
        return configFile.exists();
    }

    @Override
    public Router init() throws Exception {
        return compile();
    }

    /**
     *
     * @return compiled router
     * @throws Exception
     */
    public Router compile() throws Exception {
        HashMap<HttpRequestDef, RpcInvokerDef> mapping = parse(getFilePath());
        Router router = new Router();
        router.initialize(mapping);

        // load convertor instances into cache
        loadConvertors(mapping);
        return router;
    }

    /**
     * Load convertors as a batch to improve speed.
     * Because {@link Convertors#Cache} internally use a {@link sgw.core.util.CopyOnWriteHashMap}.
     */
    private void loadConvertors(HashMap<HttpRequestDef, RpcInvokerDef> mapping) throws Exception {
        Collection<String> httpConvertorClazzNames = mapping
                .values().stream()
                .map(invokerDef -> invokerDef.getHttpConvertorClazzName())
                .collect(Collectors.toList());
        Convertors.Cache.cacheAllConvertorsByName(httpConvertorClazzNames);
    }

    abstract protected String getFilePath();

    /**
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    abstract protected HashMap<HttpRequestDef, RpcInvokerDef> parse(String filePath) throws Exception;

}
