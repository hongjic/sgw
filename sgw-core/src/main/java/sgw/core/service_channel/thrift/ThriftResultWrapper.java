package sgw.core.service_channel.thrift;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import sgw.core.util.ChannelOrderedMessage;

public class ThriftResultWrapper implements ChannelOrderedMessage {

    private long channelResultId;
    private TBase result;
    private TApplicationException exception;

    public ThriftResultWrapper(long channelResultId, TBase result) {
        this.channelResultId = channelResultId;
        this.result = result;
    }

    public ThriftResultWrapper(long channelResultId, TApplicationException exception) {
        this.channelResultId = channelResultId;
        this.exception = exception;
    }

    @Override
    public long channelMessageId() {
        return channelResultId;
    }

    public TBase getResult() {
        return result;
    }

    public TApplicationException getException() {
         return exception;
    }
}
