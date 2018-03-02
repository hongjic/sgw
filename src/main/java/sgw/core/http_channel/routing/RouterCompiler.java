package sgw.core.http_channel.routing;

import sgw.core.data_convertor.Convertors;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;

import java.io.File;
import java.util.HashMap;

public abstract class RouterCompiler {

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

    /**
     *
     * @return compiled router
     * @throws Exception
     */
    public Router compile() throws Exception {
        HashMap<HttpRequestDef, RpcInvokerDef> mapping = parse(getFilePath());
        Router router = new Router();
        router.clearAndLoad(mapping);

        // load convertor instances into cache
        loadConvertors(mapping);
        return router;
    }

    private void loadConvertors(HashMap<HttpRequestDef, RpcInvokerDef> mapping) throws Exception {
        for (RpcInvokerDef invokerDef: mapping.values()) {
            Convertors.Cache.createReqParser(invokerDef.getRequestParser());
            Convertors.Cache.createResGen(invokerDef.getResponseGenerator());
        }
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
