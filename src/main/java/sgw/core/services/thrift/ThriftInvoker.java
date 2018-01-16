package sgw.core.services.thrift;

import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvokerDef;
import sgw.core.services.RpcInvokerManager;

/**
 * has to be thread-safe
 *
 */
public abstract class ThriftInvoker implements RpcInvoker {

    @Override
    public Object invoke(Object param) {
        String serviceName = getServiceName();
        String methodName = getMethodName();
        return null;
    }

    protected abstract String getServiceName();

    protected abstract String getMethodName();

}
