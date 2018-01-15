package sgw.core.services;

public interface RpcInvokerGenerator {

    /**
     *
     * @param invokerDef a definition of a RPC call
     * @return a RpcInvoker instance
     */
    RpcInvoker generate(RpcInvokerDef invokerDef);
}
