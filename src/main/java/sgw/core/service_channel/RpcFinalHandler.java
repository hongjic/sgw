package sgw.core.service_channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcFinalHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(RpcFinalHandler.class);

    private Channel httpChannel;

    public RpcFinalHandler(Channel httpChannel) {
        this.httpChannel = httpChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof TBase) {
            logger.info("Sending decoded thrift response back to Http channel pipeline.");
            writeBackToHttpChannel(msg);
        }
    }

    public void writeBackToHttpChannel(Object msg) {

        if (httpChannel.eventLoop().inEventLoop())
            httpChannel.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
        else
            httpChannel.eventLoop().execute(() ->
                    httpChannel.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE));
    }
}
