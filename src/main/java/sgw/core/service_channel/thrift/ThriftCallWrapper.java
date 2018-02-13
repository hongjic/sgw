package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TMessage;

/**
 * A wrapper for data necessary for a thrift call
 */
public class ThriftCallWrapper {

    private TBase args;
    private TMessage message;

    private String serviceName; // lowercase

    /**
     * @param args parameters of the thrift call
     * @param message header of thrift request
     */
    public ThriftCallWrapper(TBase args, TMessage message, String serviceName) {
        this.args = args;
        this.message = message;
        this.serviceName = serviceName;
    }

    public TBase getArgs() {
        return args;
    }

    public TMessage getMessage() {
        return message;
    }

    public String getServiceName() {
        return serviceName;
    }
}
