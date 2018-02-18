package sgw.core.filters;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;
import sgw.core.http_channel.HttpChannelContext;

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
        if (msg instanceof HttpResponse)
            httpCtx.setHttpResponse((HttpResponse) msg);
        doPostFilters(ctx);
        ctx.write(msg, promise);
    }

    private void doPostFilters(ChannelHandlerContext ctx) {
        try {
            FilterProcessor.Instance.postRouting(httpCtx);
        } catch (Exception e) {

        }
    }
}
