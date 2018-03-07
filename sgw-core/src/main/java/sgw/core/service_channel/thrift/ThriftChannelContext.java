package sgw.core.service_channel.thrift;

import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;

public class ThriftChannelContext {

    private ThriftCallWrapper callWrapper;
    private RpcInvokerDef invokerDef;
    private RpcInvoker invoker;
    private long startTime;

    public void setCallWrapper(ThriftCallWrapper wrapper) {
        callWrapper = wrapper;
    }

    public ThriftCallWrapper getCallWrapper() {
        return callWrapper;
    }

    public void setInvokerDef(RpcInvokerDef invokerDef) {
        this.invokerDef = invokerDef;
    }

    public RpcInvokerDef getInvokerDef() {
        return invokerDef;
    }

    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    public RpcInvoker getInvoker() {
        return invoker;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
