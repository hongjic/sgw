package sgw.core.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import sgw.core.http_channel.HttpRequestContext;

/**
 * used for filter customized response and operational response.
 */
public final class FastMessage implements ChannelOrderedMessage {

    private long chMsgId;
    private Exception exception;
    private HttpResponseStatus status;
    private String message;

    public FastMessage(long chMsgId, Exception e) {
        this.chMsgId = chMsgId;
        this.exception = e;
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public FastMessage(long chMSgId, String message) {
        this.chMsgId = chMSgId;
        this.message = message;
        status = HttpResponseStatus.OK;
    }

    public void setHttpResponseStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getResponseBody() {
        if (exception != null)
            return exception.getMessage();
        else
            return message;
    }

    @Override
    public long channelMessageId() {
        return chMsgId;
    }

    /**
     * Send the Fast Http Response back to the client.
     * This method will set {@link HttpRequestContext#setSendFastMessage(boolean)} to true.
     * and save the FastMessage instance in {@link HttpRequestContext}
     *
     * The difference between this method and {@link #send(Channel, HttpRequestContext)} is that
     * this method can only be invoked inside a {@link io.netty.channel.ChannelHandler} using the
     * {@link ChannelHandlerContext}, if you want to send back a response outside a handler for example
     * in a filter, you need to invoke {@link #send(Channel, HttpRequestContext)} to achieve the same
     * purpose.
     *
     * @param ctx The {@link ChannelHandlerContext} used to send back response
     * @param reqCtx http request context
     * @return A future of the write op
     */
    @Deprecated
    public ChannelFuture send(ChannelHandlerContext ctx, HttpRequestContext reqCtx) {
        beforeSend(reqCtx);
        return ctx.writeAndFlush(this);
    }

    /**
     * The same purpose with {@link #send(ChannelHandlerContext, HttpRequestContext)}.
     * See it for detail.
     * @param channel the http channel
     * @param reqCtx http request context
     * @return A future of the write op
     */
    public ChannelFuture send(Channel channel, HttpRequestContext reqCtx) {
        beforeSend(reqCtx);
        return channel.writeAndFlush(this);
    }

    private void beforeSend(HttpRequestContext reqCtx) {
        reqCtx.setSendFastMessage(true);
        reqCtx.setFastMessage(this);
    }
}
