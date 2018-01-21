package sgw.core.http_channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ResultHttpConvertor<T> extends MessageToMessageDecoder<T> {

    /**
     *
     * @param out only return a http response
     */
    @Override
    public void decode(ChannelHandlerContext ctx, T result, List<Object> out) {

    }
}