package sgw.core.service_channel.thrift;

import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;

public class ThriftInvokerDef extends RpcInvokerDef {

    private final String thriftClazz;
    private Class<?> argsClazz;
    private Class<?> resultClazz;

    public ThriftInvokerDef(RpcType protocol,
                            String serviceName,
                            String methodName,
                            String thriftClazz,
                            String requestParser,
                            String responseGenerator) {
        super(protocol, serviceName, methodName, requestParser, responseGenerator);
        this.thriftClazz = thriftClazz;
    }

    public String getThriftClazz() {
        return thriftClazz;
    }

    public String getThriftArgsClazzName() {
        return thriftClazz + "$" + methodName + "_args";
    }

    public String getThriftResultClazzName() {
        return thriftClazz + "$" + methodName + "_result";
    }

    public Class<?> getThriftArgsClazz() throws ClassNotFoundException {
        if (argsClazz == null) {
            argsClazz = Class.forName(getThriftArgsClazzName());
        }
        return argsClazz;
    }

    public Class<?> getThriftResultClazz() throws ClassNotFoundException {
        if (resultClazz == null) {
            resultClazz = Class.forName(getThriftResultClazzName());
        }
        return resultClazz;
    }

}
