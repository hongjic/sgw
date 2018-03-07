package sgw.core.service_channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcInvokerDef {

    protected final Logger logger = LoggerFactory.getLogger(RpcInvokerDef.class);

    protected final String serviceName;
    protected final String methodName;
    protected final RpcType protocol;
    protected final String httpConvertorClazzName;

    public RpcInvokerDef(RpcType protocol,
                         String serviceName,
                         String methodName,
                         String convertorClazzName) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.protocol = protocol;
        this.httpConvertorClazzName = convertorClazzName;
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

    public String getHttpConvertorClazzName() {
        return httpConvertorClazzName;
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
