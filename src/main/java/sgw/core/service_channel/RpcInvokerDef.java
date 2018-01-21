package sgw.core.service_channel;

public class RpcInvokerDef {

    private final String serviceName;
    private final String methodName;
    private final String paramConvertor;
    private final String resultConvertor;
    private final String protocol;

    public RpcInvokerDef(String serviceName, String methodName,
                         String paramConvertor, String resultConvertor, String protocol) {
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

    public String getParamConvertor() {
        return paramConvertor;
    }

    public String getResultConvertor() {
        return resultConvertor;
    }

    public String getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s, paramConvertor: %s, resultConvertor]",
                serviceName, methodName, paramConvertor, resultConvertor);
    }

    public String toSimpleString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s]",
                serviceName, methodName);
    }
}
