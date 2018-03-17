package sgw.core.http_channel.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import sgw.core.util.ChannelOrderedMessage;

public class ChannelOrderedHttpResponse implements FullHttpResponse, ChannelOrderedMessage {

    private long channelRequestId;
    private FullHttpResponse response;

    public ChannelOrderedHttpResponse(long channelRequestId, FullHttpResponse resposne) {
        this.channelRequestId = channelRequestId;
        this.response = resposne;
    }

    @Override
    public long channelMessageId() {
        return channelRequestId;
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    public HttpVersion protocolVersion() {
        return response.protocolVersion();
    }

    @Override
    @Deprecated
    public HttpVersion getProtocolVersion() {
        return response.getProtocolVersion();
    }

    @Override
    public ChannelOrderedHttpResponse setProtocolVersion(HttpVersion version) {
        response.setProtocolVersion(version);
        return this;
    }

    @Override
    public HttpResponseStatus status() {
        return response.status();
    }

    @Override
    @Deprecated
    public HttpResponseStatus getStatus() {
        return response.getStatus();
    }

    @Override
    public ChannelOrderedHttpResponse setStatus(HttpResponseStatus status) {
        response.setStatus(status);
        return this;
    }

    @Override
    public ChannelOrderedHttpResponse copy() {
        return new ChannelOrderedHttpResponse(channelRequestId, response.copy());
    }

    @Override
    public ChannelOrderedHttpResponse duplicate() {
        return new ChannelOrderedHttpResponse(channelRequestId, response.duplicate());
    }

    @Override
    public ChannelOrderedHttpResponse retainedDuplicate() {
        return new ChannelOrderedHttpResponse(channelRequestId, response.retainedDuplicate());
    }

    @Override
    public ByteBuf content() {
        return response.content();
    }

    @Override
    public ChannelOrderedHttpResponse replace(ByteBuf content) {
        return new ChannelOrderedHttpResponse(channelRequestId, response.replace(content));
    }

    @Override
    public DecoderResult decoderResult() {
        return response.decoderResult();
    }

    @Override
    @Deprecated
    public DecoderResult getDecoderResult() {
        return response.getDecoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        response.setDecoderResult(result);
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return response.trailingHeaders();
    }

    @Override
    public boolean release(int decrement) {
        return response.release(decrement);
    }

    @Override
    public boolean release() {
        return response.release();
    }

    @Override
    public ChannelOrderedHttpResponse retain() {
        response.retain();
        return this;
    }

    @Override
    public ChannelOrderedHttpResponse retain(int increment) {
        response.retain(increment);
        return this;
    }

    @Override
    public ChannelOrderedHttpResponse touch() {
        response.touch();
        return this;
    }

    @Override
    public ChannelOrderedHttpResponse touch(Object hint) {
        response.touch(hint);
        return this;
    }

    @Override
    public int refCnt() {
        return response.refCnt();
    }

}
