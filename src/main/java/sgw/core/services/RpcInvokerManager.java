package sgw.core.services;

/**
 * All implementations should be thread-safe.
 */
public interface RpcInvokerManager {

    /**
     *
     * @param invokerDef a definition of a RPC call
     * @return a RpcInvoker instance
     */
    RpcInvoker find(RpcInvokerDef invokerDef);
}
