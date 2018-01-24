package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TMessage;

/**
 * A wrapper for data necessary for a thrift call
 */
public class ThriftCallWrapper {

    private TBase args;
    private TMessage message;

    /**
     * @param args parameters of the thrift call
     * @param message header of thrift request
     */
    public ThriftCallWrapper(TBase args, TMessage message) {
        this.args = args;
        this.message = message;
    }

    public TBase getArgs() {
        return args;
    }

    public TMessage getMessage() {
        return message;
    }
}
