package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TMessage;

/**
 * A wrapper for data necessary for a thrift call
 */
public class ThriftCallWrapper {

    private TBase args;
    private TBase result;
    private TMessage message;

    private String serviceName; // lowercase, necessary for TMultiplexedProtocol

    /**
     * @param thriftArgs parameters of the thrift call
     * @param thriftResult thrift call result
     * @param message header of thrift request
     * @param serviceName thrift service name
     */
    public ThriftCallWrapper(TBase thriftArgs, TBase thriftResult, TMessage message, String serviceName) {
        this.args = thriftArgs;
        this.result = thriftResult;
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

    public TBase getResult() {
        return result;
    }
}
