package sgw.core.services;

public class RpcInvokerDef {

    private final String serviceName;
    private final String methodName;
    private final String convertor;

    public RpcInvokerDef(String serviceName, String methodName, String convertor) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.convertor = convertor;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getConvertor() {
        return convertor;
    }

    @Override
    public String toString() {
        return String.format("[serviceName: %s, methodName: %s, convertor: %s",
                serviceName, methodName, convertor);
    }
}
