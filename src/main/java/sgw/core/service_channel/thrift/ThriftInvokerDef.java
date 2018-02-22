package sgw.core.service_channel.thrift;

import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;

public class ThriftInvokerDef extends RpcInvokerDef {

    private String thriftClazz;
    private String requestParser;
    private String responseGenerator;

    public ThriftInvokerDef(RpcType protocol,
                            String serviceName,
                            String methodName,
                            String thriftClazz,
                            String requestParser,
                            String responseGenerator) {
        super(serviceName, methodName, protocol);
        this.thriftClazz = thriftClazz;
        this.requestParser = requestParser;
        this.responseGenerator = responseGenerator;
    }

    public String getThriftClazz() {
        return thriftClazz;
    }

    public String getThriftArgsClazz() {
        return thriftClazz + "$" + methodName + "_args";
    }

    public String getThriftResultClazz() {
        return thriftClazz + "$" + methodName + "_result";
    }

    public String getRequestParser() {
        return requestParser;
    }

    public String getResponseGenerator() {
        return responseGenerator;
    }
}
