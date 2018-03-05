package sgw.core.routing;

import io.netty.handler.codec.http.HttpMethod;
import org.yaml.snakeyaml.Yaml;
import sgw.core.http_channel.HttpRequestDef;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;
import sgw.core.service_channel.thrift.ThriftInvokerDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

public class YamlRouterCompiler extends RouterCompiler {

    private static final String DEFAULT_PATH = "src/main/resources/routing.yaml";

    private String path;

    public YamlRouterCompiler(String filePath) {
        path = filePath;
    }

    @Override
    protected HashMap<HttpRequestDef, RpcInvokerDef> parse(String filePath)
            throws FileNotFoundException, ClassNotFoundException {
        if (configFile == null)
            configFile = new File(filePath);
        Yaml yaml = new Yaml();
        RoutingData data = yaml.loadAs(new FileInputStream(configFile), RoutingData.class);
        HashMap<HttpRequestDef, RpcInvokerDef> mapping = new HashMap<>();

        // parse thrift service routing
        parseThrift(mapping, data);

        return mapping;
    }

    private void parseThrift(HashMap<HttpRequestDef, RpcInvokerDef> mapping, RoutingData data) throws ClassNotFoundException {
        for (ThriftAPI api: data.getThriftServices()) {
            String[] strs = api.getHttp().split(" ");
            HttpRequestDef httpDef = new HttpRequestDef(HttpMethod.valueOf(strs[0]), strs[1]);
            ThriftInvokerDef thriftDef = new ThriftInvokerDef(
                    RpcType.Thrift,
                    api.getService(),
                    api.getMethod(),
                    api.getClazz(),
                    api.getDupleConvertor() == null ? api.getRequestParser() : api.getDupleConvertor(),
                    api.getDupleConvertor() == null ? api.getResponseGenerator() : api.getDupleConvertor());
            mapping.put(httpDef, thriftDef);
        }
    }

    @Override
    protected String getFilePath() {
        return path == null ? DEFAULT_PATH : path;
    }

    public static class RoutingData {
        private List<ThriftAPI> thriftServices;

        public void setThriftServices(List<ThriftAPI> thriftServices) {
            this.thriftServices = thriftServices;
        }

        public List<ThriftAPI> getThriftServices() {
            return thriftServices;
        }
    }

    public static class ThriftAPI {
        private String http;
        private String service;
        private String method;
        private String clazz;
        private String requestParser;
        private String responseGenerator;
        private String dupleConvertor;

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public void setDupleConvertor(String dupleConvertor) {
            this.dupleConvertor = dupleConvertor;
        }

        public void setHttp(String http) {
            this.http = http;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void setRequestParser(String requestParser) {
            this.requestParser = requestParser;
        }

        public void setResponseGenerator(String responseGenerator) {
            this.responseGenerator = responseGenerator;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getClazz() {
            return clazz;
        }

        public String getDupleConvertor() {
            return dupleConvertor;
        }

        public String getHttp() {
            return http;
        }

        public String getMethod() {
            return method;
        }

        public String getRequestParser() {
            return requestParser;
        }

        public String getResponseGenerator() {
            return responseGenerator;
        }

        public String getService() {
            return service;
        }

    }

}
