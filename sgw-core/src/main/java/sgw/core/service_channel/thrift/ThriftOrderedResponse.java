package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import sgw.core.util.ChannelOrderedMessage;

public class ThriftOrderedResponse implements ChannelOrderedMessage {

    private TBase result;
    private long channelRequestId;

    @Override
    public long channelMessageId() {
        return channelRequestId;
    }

    public void setChannelRequestId(long channelRequestId) {
        this.channelRequestId = channelRequestId;
    }

    public void setResult(TBase result) {
        this.result = result;
    }

    public TBase getResult() {
        return result;
    }
}
