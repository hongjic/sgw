package sgw.core.services.thrift;

import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvokerDef;
import sgw.core.services.RpcInvokerManager;

/**
 * Only created once, need to be thread-safe.
 * TODO: finish implementation. Make sure compatibility with MoSeeker service discovery functions;
 */
public class ThriftServiceManager implements RpcInvokerManager {

    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) {
        return null;
    }
}
