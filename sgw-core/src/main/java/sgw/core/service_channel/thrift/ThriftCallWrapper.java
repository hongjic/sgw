package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.util.ChannelOrderedMessage;

/**
 * A wrapper for data necessary for a thrift call
 */
public class ThriftCallWrapper implements ChannelOrderedMessage {

    private HttpRequestContext httpReqCtx;
    private TBase args;
    private String serviceName; // lowercase, necessary for TMultiplexedProtocol
    private String methodName;

    /**
     * @param httpReqCtx http request context.
     * @param thriftArgs parameters of the thrift call
     * @param serviceName thrift service name
     * @param methodName thrift method name
     */
    public ThriftCallWrapper(HttpRequestContext httpReqCtx, TBase thriftArgs, String serviceName, String methodName) {
        this.httpReqCtx = httpReqCtx;
        this.args = thriftArgs;
        this.serviceName = serviceName;
        this.methodName = methodName;
    }

    /**
     *
     * @return The corresponding http request id in the http channel.
     */
    @Override
    public long channelMessageId() {
        return httpReqCtx.getChannelRequestId();
    }

    public TBase getArgs() {
        return args;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public HttpRequestContext getHttpReqCtx() {
        return httpReqCtx;
    }
}
