package sgw.core.service_channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.thrift.TBase;

public class RpcFinalHandler extends ChannelInboundHandlerAdapter {

    private Channel httpChannel;

    public RpcFinalHandler(Channel httpChannel) {
        this.httpChannel = httpChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof TBase) {
            writeBackToHttpChannel(msg);
        }
    }

    public void writeBackToHttpChannel(Object msg) {
        if (httpChannel.eventLoop().inEventLoop())
            httpChannel.writeAndFlush(msg);
        else
            httpChannel.eventLoop().execute(() -> httpChannel.writeAndFlush(msg));
    }
}
