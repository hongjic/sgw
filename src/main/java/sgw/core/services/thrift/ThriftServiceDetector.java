package sgw.core.services.thrift;

import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvokerDef;
import sgw.core.services.RpcInvokerDetector;

/**
 * Only created once, need to be thread-safe.
 * TODO: finish  implementation
 */
public abstract class ThriftServiceDetector implements RpcInvokerDetector {

    @Override
    public RpcInvoker find(RpcInvokerDef invokerDef) {
        // TODO: use service discovery.
        // temporary hard code here.


        return null;
    }
}
