package sgw.core.filters;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpChannelInitializer;

public class PostRoutingFiltersHandler extends ChannelOutboundHandlerAdapter {

    private HttpChannelContext httpCtx;

    public PostRoutingFiltersHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        /**
         * msg type: FullHttpResponse
         */
        FullHttpResponse response;
        if (msg instanceof FullHttpResponse) {
            response = (FullHttpResponse) msg;
            httpCtx.setHttpResponse(response);
        }
        boolean continueProcessing = doPostFilters(ctx);
        if (continueProcessing)
            ctx.write(msg, promise);
        else {
            int refCnt = ReferenceCountUtil.refCnt(msg);
            System.out.println("Post filters release msg refCnt: " + refCnt);
            ReferenceCountUtil.release(msg, refCnt);
        }
    }

    /**
     * @return true if to continue processing
     */
    private boolean doPostFilters(ChannelHandlerContext ctx) {
        try {
            FilterProcessor.Instance.postRouting(httpCtx);
        } catch (AbstractFilter.FilterException e) {
            sendFastResponse(ctx);
            return false;
        }

        boolean sendFastMessage = httpCtx.getSendFastMessage();
        if (sendFastMessage) {
            sendFastResponse(ctx);
        }

        return !sendFastMessage;
    }

    private void sendFastResponse(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        // modify pipeline
        pipeline.replace(HttpChannelInitializer.RESPONSE_CONVERTOR,
                HttpChannelInitializer.RESPONSE_CONVERTOR, new FastMessageToHttpRsp());
        pipeline.remove(HttpChannelInitializer.POST_FILTER);
        // skip other inbound handlers, write response directly
        FastMessage message = httpCtx.getFastMessage();
        if (message == null)
            message = FastMessage.EMPTY;
        ctx.channel().writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
    }
}
