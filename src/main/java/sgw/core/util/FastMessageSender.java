package sgw.core.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import sgw.core.http_channel.FastMessageToHttpRsp;
import sgw.core.http_channel.HttpChannelInitializer;

public class FastMessageSender {

    /**
     * Always invoke {@link sgw.core.http_channel.HttpChannelContext#setSendFastMessage(boolean)}, set to true
     * when you invoke this method.
     * @param ctx
     * @param message {@link FastMessage} generated either because of filters or exceptions caught
     * @return http channel future
     */
    public static ChannelFuture send(ChannelHandlerContext ctx, FastMessage message) {
        return send(ctx.channel(), message);
    }

    /**
     * Always invoke {@link sgw.core.http_channel.HttpChannelContext#setSendFastMessage(boolean)}, set to true
     * when you invoke this method.
     * @param channel
     * @param message {@link FastMessage} generated either because of filters or exceptions caught
     * @return http channel future
     */
    public static ChannelFuture send(Channel channel, FastMessage message) {
        ChannelPipeline pipeline = channel.pipeline();
        //modify pipeline
        if (!(pipeline.get(HttpChannelInitializer.RESPONSE_CONVERTOR) instanceof FastMessageToHttpRsp)) {
            pipeline.replace(HttpChannelInitializer.RESPONSE_CONVERTOR,
                    HttpChannelInitializer.RESPONSE_CONVERTOR,
                    new FastMessageToHttpRsp());
        }
        // skip other inbound handlers, write response directly
        if (message == null)
            message = FastMessage.EMPTY;

        return channel.writeAndFlush(message);
    }
}
