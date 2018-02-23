package sgw.core.service_channel;

public class RpcInvokerDef {

    protected final String serviceName;
    protected final String methodName;
    protected final RpcType protocol;
    protected final String requestParser;
    protected final String responseGenerator;

    public RpcInvokerDef(RpcType protocol,
                         String serviceName,
                         String methodName,
                         String requestParser,
                         String responseGenerator) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.protocol = protocol;
        this.requestParser = requestParser;
        this.responseGenerator = responseGenerator;
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

    public String getRequestParser() {
        return requestParser;
    }

    public String getResponseGenerator() {
        return responseGenerator;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = (h * 31) + serviceName.hashCode();
        h = (h * 31) + methodName.hashCode();
        h = (h * 31) + protocol.hashCode();
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RpcInvokerDef))
            return false;
        RpcInvokerDef other = (RpcInvokerDef) o;
        return other.serviceName.equals(serviceName) &&
                other.methodName.equals(methodName) &&
                other.protocol.equals(protocol);
    }

    @Override
    public String toString() {
        return String.format("[protocol: %s, serviceName: %s, methodName: %s]",
                protocol.toString(), serviceName, methodName);
    }

}
