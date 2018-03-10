package sgw.core.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpResponseStatus;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpChannelInitializer;

/**
 * used for filter customized response and operational response.
 */
public final class FastMessage {

    private Exception exception;
    private HttpResponseStatus status;

    private String message;

    static FastMessage emptyMessage() {
        FastMessage message = new FastMessage("Request filtered, but no response message specified.");
        message.setHttpResponseStatus(HttpResponseStatus.OK);
        return message;
    }

    public static final FastMessage EMPTY = emptyMessage();

    public FastMessage(Exception e) {
        this.exception = e;
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public FastMessage(String message) {
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

    /**
     * Send the Fast Http Response back to the client.
     * This method will change the ChannelPipeline, use {@link FastMessageToHttpRsp} to convert {@link FastMessage}
     * to {@link io.netty.handler.codec.http.FullHttpResponse} and invoke {@code httpCtx.setFastMessage(true)}.
     *
     * The difference between this method and {@link #send(Channel, HttpChannelContext)} is that
     * this method can only be invoked inside a {@link io.netty.channel.ChannelHandler} using the
     * {@link ChannelHandlerContext}, if you want to send back a response outside a handler for example
     * in a filter, you need to invoke {@link #send(Channel, HttpChannelContext)} to achieve the same
     * purpose.
     *
     * @param ctx The {@link ChannelHandlerContext} used to send back response
     * @param httpCtx Always set {@link HttpChannelContext#setSendFastMessage(boolean)} to true and set the
     *                {@link FastMessage} field.
     * @return A future of the write op
     */
    public ChannelFuture send(ChannelHandlerContext ctx, HttpChannelContext httpCtx) {
        beforeSend(ctx.pipeline(), httpCtx);
        return ctx.writeAndFlush(this);
    }

    /**
     * The same purpose with {@link #send(ChannelHandlerContext, HttpChannelContext)}.
     * See it for detail.
     * @param channel the http channel
     * @param httpCtx http channel context
     * @return A future of the write op
     */
    public ChannelFuture send(Channel channel, HttpChannelContext httpCtx) {
        beforeSend(channel.pipeline(), httpCtx);
        return channel.writeAndFlush(this);
    }

    private void beforeSend(ChannelPipeline pipeline, HttpChannelContext httpCtx) {
        httpCtx.setSendFastMessage(true);
        httpCtx.setFastMessage(this);
        //modify pipeline
        if (!(pipeline.get(HttpChannelInitializer.RESPONSE_CONVERTOR) instanceof FastMessageToHttpRsp)) {
            pipeline.replace(HttpChannelInitializer.RESPONSE_CONVERTOR,
                    HttpChannelInitializer.RESPONSE_CONVERTOR,
                    new FastMessageToHttpRsp(httpCtx));
        }
    }
}
