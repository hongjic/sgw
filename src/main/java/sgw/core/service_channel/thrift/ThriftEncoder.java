package sgw.core.service_channel.thrift;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.thrift.TBase;

public class ThriftEncoder<T extends TBase> extends MessageToByteEncoder<TWrapper<T>> {

    @Override
    public void encode(ChannelHandlerContext ctx, TWrapper<T> wrapper, ByteBuf out) {


    }
}
