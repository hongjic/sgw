package sgw.core.service_channel;

import java.util.HashMap;

/**
 * No synchronization is needed since all ops on a single request runs in the same thread.
 */
public abstract class RpcRequestContext extends HashMap<String, Object> {

    private long httpGlRequestId;
    private long httpChRequestId;
    private long rpcChRequestId;
    private RpcInvoker invoker;
    private RpcInvokerDef invokerDef;

    public long getHttpGlRequestId() {
        return httpGlRequestId;
    }

    public void setHttpGlRequestId(long id) {
        this.httpGlRequestId = id;
    }

    public long getHttpChRequestId() {
        return httpChRequestId;
    }

    public void setHttpChRequestId(long id) {
        this.httpChRequestId = id;
    }

    public long getRpcChRequestId() {
        return rpcChRequestId;
    }

    public void setRpcChRequestId(long id) {
        this.rpcChRequestId = id;
    }

    public RpcInvoker getRpcInvoker() {
        return invoker;
    }

    public void setRpcInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    public RpcInvokerDef getRpcInvokerDef() {
        return invokerDef;
    }

    public void setRpcInvokerDef(RpcInvokerDef invokerDef) {
        this.invokerDef = invokerDef;
    }

}
