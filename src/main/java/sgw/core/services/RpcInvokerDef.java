package sgw.core.services;

public class RpcInvokerDef {

    private final String serviceName;
    private final String methodName;
    private final String paramConvertor;
    private final String resultConvertor;

    public RpcInvokerDef(String serviceName, String methodName, String paramConvertor, String resultConvertor) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.paramConvertor = paramConvertor;
        this.resultConvertor = resultConvertor;
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

    @Override
    public String toString() {
        return String.format("[serviceName: %s, methodName: %s, paramConvertor: %s, resultConvertor",
                serviceName, methodName, paramConvertor, resultConvertor);
    }

    public String toSimpleString() {
        return String.format("[serviceName: %s, methodName: %s]",
                serviceName, methodName);
    }
}
