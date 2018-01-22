package sgw.core.http_channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.thrift.TBase;

import java.util.List;

public class ResultHttpConvertor extends MessageToMessageEncoder<TBase> {

    /**
     *
     * @param out only return a http response
     */
    @Override
    public void encode(ChannelHandlerContext ctx, TBase result, List<Object> out) {
        System.out.println("Hello Im here!");
    }
}