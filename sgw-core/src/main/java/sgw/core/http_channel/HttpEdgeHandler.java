package sgw.core.http_channel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import sgw.core.filters.FilterException;
import sgw.core.filters.FilterProcessor;
import sgw.core.http_channel.util.ChannelOrderedHttpRequest;
import sgw.core.http_channel.util.ChannelOrderedHttpResponse;

public class HttpEdgeHandler extends ChannelDuplexHandler {

    private final HttpChannelContext chanCtx;
    private long maxRequestPerChannel;

    HttpEdgeHandler(HttpChannelContext chanCtx) {
        this.chanCtx = chanCtx;
        this.maxRequestPerChannel = chanCtx.getConfig().getMaxRequestPerHttpConnection();
        // 0 means no upper limit.
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        /**
         * type of msg: {@link io.netty.handler.codec.http.FullHttpRequest}
         */
        assert (msg instanceof FullHttpRequest);

        HttpRequestContext reqCtx = chanCtx.newRequestContext();
        long chReqId = reqCtx.getChannelRequestId();
        ChannelOrderedHttpRequest orderedRequest = new ChannelOrderedHttpRequest(chReqId, (FullHttpRequest) msg);
        reqCtx.setHttpRequest(orderedRequest);
        ReferenceCountUtil.retain(orderedRequest);

        boolean continueProcessing = doPreFilter(ctx, reqCtx);
        if (continueProcessing)
            ctx.fireChannelRead(orderedRequest);
        else {
            int refCnt = ReferenceCountUtil.refCnt(msg);
            ReferenceCountUtil.release(msg, refCnt);
        }

    }

    /**
     * When reached maxRequestPerChannel, append "Connection: Close" in the last response
     * and close the channel.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        /**
         * msg: {@link ChannelOrderedHttpResponse}
         */
        assert (msg instanceof ChannelOrderedHttpResponse);
        ChannelOrderedHttpResponse orderedResponse = (ChannelOrderedHttpResponse) msg;

        long chReqId = orderedResponse.channelMessageId();
        HttpRequestContext reqCtx = chanCtx.getRequestContext(chReqId);
        reqCtx.setHttpResponse(orderedResponse);
        boolean continueProcessing = doPostFilter(ctx, reqCtx);
        releaseHttpMessage(reqCtx.getHttpRequest());

        if (continueProcessing) {
            // reach upper request limit per channel
            // ready to close connection
            if (maxRequestPerChannel > 0 && chReqId >= maxRequestPerChannel) {
                HttpUtil.setKeepAlive(orderedResponse, false);
                promise.addListener(ChannelFutureListener.CLOSE);
            }
            chanCtx.removeRequestContext(chReqId);
            ctx.write(orderedResponse, promise);
        }
        else {
            releaseHttpMessage(orderedResponse);
        }

    }

    private void releaseHttpMessage(FullHttpMessage message) {
        int refCnt = ReferenceCountUtil.refCnt(message);
        ReferenceCountUtil.release(message, refCnt);
    }

    /**
     * @return true if to continue processing
     */
    private boolean doPreFilter(ChannelHandlerContext ctx, HttpRequestContext reqCtx) {
        try {
            FilterProcessor.Instance.preRouting(reqCtx);
        } catch (FilterException e) {
            ctx.fireExceptionCaught(e);
            return false;
        }
        return true;
    }

    /**
     * @return true if to continue processing
     */
    private boolean doPostFilter(ChannelHandlerContext ctx, HttpRequestContext reqCtx) {
        try {
            FilterProcessor.Instance.postRouting(reqCtx);
        } catch (FilterException e) {
            if (reqCtx.getSendFastMessage())
                return true;
            ctx.fireExceptionCaught(e);
            return false;
        }
        return true;
    }

}
