package sgw.core.service_channel.thrift;

import sgw.core.service_channel.RpcRequestContext;

public class ThriftRequestContext extends RpcRequestContext {

    private ThriftOrderedRequest request;

    public void setRequest(ThriftOrderedRequest request) {
        this.request = request;
    }

    public ThriftOrderedRequest getRequest() {
        return request;
    }
}
