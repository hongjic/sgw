package sgw.core.services;

public interface RpcInvokerManager {

    /**
     *
     * @param invokerDef a definition of a RPC call
     * @return a RpcInvoker instance
     */
    RpcInvoker find(RpcInvokerDef invokerDef);
}
