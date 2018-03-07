package sgw.core.filters;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.filters.AbstractFilter.FilterException;
import sgw.core.http_channel.HttpChannelInitializer;
import sgw.core.util.FastMessageSender;

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
            ChannelFuture future = FastMessageSender.send(ctx, httpCtx.getFastMessage());
            future.addListener(ChannelFutureListener.CLOSE);
            return false;
        }
        // continue check in `httpCtx` if request has to stop
        boolean sendFastMessage = httpCtx.getSendFastMessage();
        if (sendFastMessage) {
            ChannelFuture future = FastMessageSender.send(ctx, httpCtx.getFastMessage());
            future.addListener(ChannelFutureListener.CLOSE);
        }

        return !sendFastMessage;
    }

}
