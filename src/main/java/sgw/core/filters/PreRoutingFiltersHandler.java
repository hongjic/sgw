package sgw.core.filters;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.filters.AbstractFilter.FilterException;

public class PreRoutingFiltersHandler extends ChannelInboundHandlerAdapter {

    private HttpChannelContext httpCtx;

    public PreRoutingFiltersHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        /**
         * msg type: {@link io.netty.handler.codec.http.FullHttpRequest}
         */
        if (msg instanceof HttpRequest)
            httpCtx.setHttpRequest((HttpRequest) msg);
        boolean continueProcessing = doPreFilter(ctx);
        if (continueProcessing)
            ctx.fireChannelRead(msg);
    }

    /**
     * @return true if to continue processing
     */
    private boolean doPreFilter(ChannelHandlerContext ctx) {
        try {
            FilterProcessor.Instance.preRouting(httpCtx);
        } catch (FilterException e) {
            FastResponseSender.sendFilterErrorResponse(ctx, e);
        }
        // continue check in `httpCtx` if request has to stop
        boolean continueProcessing = httpCtx.getContinueProcessing();
        if (!continueProcessing) {
            FastResponseMessage message = httpCtx.getFastResponseMessage();
            FastResponseSender.sendFilterSuccessResponse(ctx, message);
        }

        return continueProcessing;
    }

}
