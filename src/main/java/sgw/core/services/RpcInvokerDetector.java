package sgw.core.services;

/**
 * An interface for the basic functionality of service discovery.
 * Enable different implementations. All implementations should be
 * thread-safe.
 */
public interface RpcInvokerDetector {
     /**
      *
      * @param invokerDef a definition of a RPC call
      * @return a RpcInvoker instance
      */
     RpcInvoker find(RpcInvokerDef invokerDef) throws Exception;

}
