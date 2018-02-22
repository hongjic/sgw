package sgw.core.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelInitializer;

public class FastMessageSender {

    public static ChannelFuture send(ChannelHandlerContext ctx, FastMessage message) {
        ChannelPipeline pipeline = ctx.pipeline();
        // modify pipeline
        if (!(pipeline.get(HttpChannelInitializer.RESPONSE_CONVERTOR) instanceof FastMessageToHttpRsp)) {
            pipeline.replace(HttpChannelInitializer.RESPONSE_CONVERTOR,
                    HttpChannelInitializer.RESPONSE_CONVERTOR,
                    new FastMessageToHttpRsp());
        }
        // skip other inbound handlers, write response directly
        if (message == null)
            message = FastMessage.EMPTY;
        return ctx.channel().writeAndFlush(message);
    }
}