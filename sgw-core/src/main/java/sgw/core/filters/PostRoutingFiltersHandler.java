package sgw.core.filters;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpChannelInitializer;
import sgw.core.util.FastMessageSender;

public class PostRoutingFiltersHandler extends ChannelOutboundHandlerAdapter {

    private HttpChannelContext httpCtx;

    public PostRoutingFiltersHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        httpCtx.put("post_filters_start", System.currentTimeMillis());
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
        if (httpCtx.getSendFastMessage()) {
            try {
                FilterProcessor.Instance.postRouting(httpCtx);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.close();
                return false;
            }
            return true;
        }

        try {
            FilterProcessor.Instance.postRouting(httpCtx);
        } catch (AbstractFilter.FilterException e) {
            ChannelFuture future = FastMessageSender.send(ctx, httpCtx.getFastMessage());
            future.addListener(ChannelFutureListener.CLOSE);
            return false;
        }

        boolean sendFastMessage = httpCtx.getSendFastMessage();
        if (sendFastMessage) {
            ChannelFuture future = FastMessageSender.send(ctx, httpCtx.getFastMessage());
            future.addListener(ChannelFutureListener.CLOSE);
        }

        return !sendFastMessage;
    }

}
