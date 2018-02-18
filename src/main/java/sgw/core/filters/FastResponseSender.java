package sgw.core.filters;

import io.netty.channel.ChannelHandlerContext;
import sgw.core.filters.AbstractFilter.FilterException;

public class FastResponseSender {

    public static void sendFilterErrorResponse(ChannelHandlerContext ctx, FilterException e) {
        // TODO: send back filter error response

    }

    public static void sendFilterSuccessResponse(ChannelHandlerContext ctx, FastResponseMessage messsage) {
        // TODO: send back filter success response
    }
}
