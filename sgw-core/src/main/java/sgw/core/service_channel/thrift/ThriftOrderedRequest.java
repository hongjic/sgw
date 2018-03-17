package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TMessage;
import sgw.core.util.ChannelOrderedMessage;

public class ThriftOrderedRequest implements ChannelOrderedMessage {

    private TBase args;
    private String serviceName;
    private String methodName;
    private long channelRequestId;

    public ThriftOrderedRequest() { }

    public void setArgs(TBase args) {
        this.args = args;
    }

    public TBase getArgs() {
        return args;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setChannelRequestId(long channelRequestId) {
        this.channelRequestId = channelRequestId;
    }

    @Override
    public long channelMessageId() {
        return channelRequestId;
    }
}
