package sgw.core.http_channel.routing_;

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
        HashMap<HttpRequestDef, RpcInvokerDef> map = parse(getFilePath());
        Router router = new Router();
        router.clearAndLoad(map);
        return router;
    }

    abstract protected String getFilePath();

    abstract protected HashMap<HttpRequestDef, RpcInvokerDef> parse(String filePath) throws Exception;

}
