package examples.netty.timeserver;

import examples.netty.data.UnixTime;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TimeEncoder extends MessageToByteEncoder<UnixTime> {

    @Override
    protected void encode(ChannelHandlerContext tx, UnixTime msg, ByteBuf out) {
        out.writeInt((int) msg.value());
    }
}
