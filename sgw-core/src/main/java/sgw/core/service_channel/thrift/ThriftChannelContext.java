package sgw.core.service_channel.thrift;

import sgw.core.service_channel.RpcChannelContext;

public class ThriftChannelContext extends RpcChannelContext {

    private ThriftCallWrapper callWrapper;

    public void setCallWrapper(ThriftCallWrapper wrapper) {
        callWrapper = wrapper;
    }

    public ThriftCallWrapper getCallWrapper() {
        return callWrapper;
    }

}
