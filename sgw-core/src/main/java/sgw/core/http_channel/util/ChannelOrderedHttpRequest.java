package sgw.core.http_channel.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import sgw.core.util.ChannelOrderedMessage;

public class ChannelOrderedHttpRequest implements FullHttpRequest, ChannelOrderedMessage {

    private long channelRequestId;
    private FullHttpRequest request;

    public ChannelOrderedHttpRequest(long channelRequestId, FullHttpRequest request) {
        this.channelRequestId = channelRequestId;
        this.request = request;
    }

    @Override
    public long channelMessageId() {
        return channelRequestId;
    }

    @Override
    public HttpHeaders headers() {
        return request.headers();
    }

    @Override
    public HttpVersion protocolVersion() {
        return request.protocolVersion();
    }

    @Override
    @Deprecated
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public ChannelOrderedHttpRequest setProtocolVersion(HttpVersion version) {
        request.setProtocolVersion(version);
        return this;
    }

    @Override
    public HttpMethod method() {
        return request.method();
    }

    @Override
    @Deprecated
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public ChannelOrderedHttpRequest setMethod(HttpMethod method) {
        request.setMethod(method);
        return this;
    }

    @Override
    @Deprecated
    public String getUri() {
        return request.getUri();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        request.setUri(uri);
        return this;
    }

    @Override
    public ChannelOrderedHttpRequest copy() {
        return new ChannelOrderedHttpRequest(channelRequestId, request.copy());
    }

    @Override
    public ChannelOrderedHttpRequest duplicate() {
        return new ChannelOrderedHttpRequest(channelRequestId, request.duplicate());
    }

    @Override
    public ChannelOrderedHttpRequest retainedDuplicate() {
        return new ChannelOrderedHttpRequest(channelRequestId, request.retainedDuplicate());
    }

    @Override
    public ByteBuf content() {
        return request.content();
    }

    @Override
    public ChannelOrderedHttpRequest replace(ByteBuf content) {
        return new ChannelOrderedHttpRequest(channelRequestId, request.replace(content));
    }

    @Override
    @Deprecated
    public DecoderResult getDecoderResult() {
        return request.getDecoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return request.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        request.setDecoderResult(result);
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return request.trailingHeaders();
    }

    @Override
    public boolean release() {
        return request.release();
    }

    @Override
    public boolean release(int decrement) {
        return request.release(decrement);
    }

    @Override
    public int refCnt() {
        return request.refCnt();
    }

    @Override
    public ChannelOrderedHttpRequest retain() {
        request.retain();
        return this;
    }

    @Override
    public ChannelOrderedHttpRequest retain(int increment) {
        request.retain(increment);
        return this;
    }

    @Override
    public ChannelOrderedHttpRequest touch() {
        request.touch();
        return this;
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        request.touch(hint);
        return this;
    }


}
