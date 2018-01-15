package sgw.core.services;

public class RpcInvokerDef {

    private final String serviceName;

    public RpcInvokerDef(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
