package sgw.core.service_channel;

public class RpcInvokerDef {

    protected final String serviceName;
    protected final String methodName;
    protected final RpcType protocol;

    public RpcInvokerDef(String serviceName, String methodName, RpcType protocol) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.protocol = protocol;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public RpcType getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s]",
                protocol.toString(), serviceName, methodName);
    }

}
