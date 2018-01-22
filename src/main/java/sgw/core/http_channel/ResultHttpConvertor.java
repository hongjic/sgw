package sgw.core.http_channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.thrift.TBase;

import java.util.List;

public class ResultHttpConvertor extends MessageToMessageDecoder<TBase> {

    /**
     *
     * @param out only return a http response
     */
    @Override
    public void decode(ChannelHandlerContext ctx, TBase result, List<Object> out) {

    }
}