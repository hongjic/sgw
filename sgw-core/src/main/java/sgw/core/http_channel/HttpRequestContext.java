package sgw.core.http_channel;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.util.FastMessage;

import java.util.HashMap;

/**
 * No synchronization is needed since all ops on a single request runs in the same thread.
 */
public class HttpRequestContext extends HashMap<String, Object> {

    private long glRequestId;
    private long chRequestId;
    private FullHttpRequest request;
    private FullHttpResponse response;
    private boolean sendFastMessage;
    private FastMessage fastMessage;
    private RpcInvoker invoker;
    private RpcInvokerDef invokerDef;
    private FullHttpRequestParser requestParser;
    private FullHttpResponseGenerator responseGenerator;

    HttpRequestContext(long globalRequestId, long channelRequestId) {
        super();
        this.glRequestId = globalRequestId;
        this.chRequestId = channelRequestId;
    }

    /**
     *
     * @return global request id (starts from 1)
     */
    public long getGlobalRequestId() {
        return glRequestId;
    }

    /**
     *
     * @return channel request id (starts from 1)
     */
    public long getChannelRequestId() {
        return chRequestId;
    }

    public FullHttpRequest getHttpRequest() {
        return request;
    }

    public void setHttpRequest(FullHttpRequest httpRequest) {
        this.request = httpRequest;
    }

    public FullHttpResponse getHttpResponse() {
        return response;
    }

    public void setHttpResponse(FullHttpResponse httpResponse) {
        this.response = httpResponse;
    }

    public boolean getSendFastMessage() {
        return sendFastMessage;
    }

    public void setSendFastMessage(boolean sendFastMessage) {
        this.sendFastMessage = sendFastMessage;
    }

    public void setFastMessage(FastMessage message) {
        this.fastMessage = message;
    }

    public FastMessage getFastMessage() {
        return fastMessage;
    }

    public RpcInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(RpcInvoker invoker) {
        this.invoker = invoker;
    }

    public void setInvokerDef(RpcInvokerDef invokerDef) {
        this.invokerDef = invokerDef;
    }

    public RpcInvokerDef getInvokerDef() {
        return invokerDef;
    }

    public FullHttpRequestParser getHttpRequestParser() {
        return requestParser;
    }

    public void setHttpRequestParser(FullHttpRequestParser parser) {
        this.requestParser = parser;
    }

    public FullHttpResponseGenerator getHttpResponseGenerator() {
        return responseGenerator;
    }

    public void setHttpResponseGenerator(FullHttpResponseGenerator responseGenerator) {
        this.responseGenerator = responseGenerator;
    }

}
