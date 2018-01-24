package sgw.core.service_channel;

import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;

public class RpcInvokerDef {

    private final String serviceName;
    private final String methodName;
    private final FullHttpRequestParser paramConvertor;
    private final FullHttpResponseGenerator resultConvertor;
    private final RpcType protocol;

    public RpcInvokerDef(String serviceName, String methodName,
                         FullHttpRequestParser paramConvertor,
                         FullHttpResponseGenerator resultConvertor,
                         RpcType protocol) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.paramConvertor = paramConvertor;
        this.resultConvertor = resultConvertor;
        this.protocol = protocol;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public FullHttpRequestParser getParamConvertor() {
        return paramConvertor;
    }

    public FullHttpResponseGenerator getResultConvertor() {
        return resultConvertor;
    }

    public RpcType getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s, paramConvertor: %s, resultConvertor]",
                protocol.toString(), serviceName, methodName,
                paramConvertor.getClass().getName(),
                resultConvertor.getClass().getName());
    }

    public String toSimpleString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s]",
                protocol.toString(), serviceName, methodName);
    }
}
