package sgw.core.filters;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.filters.AbstractFilter.FilterException;
import sgw.core.http_channel.HttpChannelInitializer;

public class PreRoutingFiltersHandler extends ChannelInboundHandlerAdapter {

    private HttpChannelContext httpCtx;

    public PreRoutingFiltersHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        /**
         * msg type: {@link FullHttpRequest}
         */
        FullHttpRequest request;
        if (msg instanceof FullHttpRequest) {
            request = (FullHttpRequest) msg;
            httpCtx.setHttpRequest(request);
        }
        boolean continueProcessing = doPreFilter(ctx);
        if (continueProcessing)
            ctx.fireChannelRead(msg);
        else {
            int refCnt = ReferenceCountUtil.refCnt(msg);
            System.out.println("Pre filters release msg refCnt: " + refCnt);
            ReferenceCountUtil.release(msg, refCnt);
        }
    }

    /**
     * @return true if to continue processing
     */
    private boolean doPreFilter(ChannelHandlerContext ctx) {

        try {
            FilterProcessor.Instance.preRouting(httpCtx);
        } catch (FilterException e) {
            sendFastResponse(ctx, new FastMessage(e));
            return false;
        }
        // continue check in `httpCtx` if request has to stop
        boolean continueProcessing = httpCtx.getContinueProcessing();
        if (!continueProcessing) {
            FastMessage message = httpCtx.getFastMessage();
            if (message == null)
                message = FastMessage.EMPTY;
            sendFastResponse(ctx, message);
        }

        return continueProcessing;
    }

    private void sendFastResponse(ChannelHandlerContext ctx, FastMessage message) {
        ChannelPipeline pipeline = ctx.pipeline();
        // modify pipeline
        pipeline.replace(HttpChannelInitializer.RESPONSE_CONVERTOR,
                HttpChannelInitializer.RESPONSE_CONVERTOR, new FastMessageToHttpRsp());
        pipeline.remove(HttpChannelInitializer.POST_FILTER);
        // skip other inbound handlers, write response directly
        ctx.channel().writeAndFlush(message).addListener(ChannelFutureListener.CLOSE);
    }

}
