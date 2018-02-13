package sgw.core.service_channel.thrift;

public class ThriftChannelContext {

    private ThriftCallWrapper callWrapper;

    public void setCallWrapper(ThriftCallWrapper wrapper) {
        callWrapper = wrapper;
    }

    public ThriftCallWrapper getCallWrapper() {
        return callWrapper;
    }
}
