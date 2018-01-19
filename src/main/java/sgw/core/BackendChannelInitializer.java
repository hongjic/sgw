package sgw.core;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import sgw.core.services.RpcInvokerDef;

import java.util.List;


/**
 * Unlike {@link HttpChannelInitializer}, it is not shared among threads.
 * Every new RPC connection will create a brand new {@link BackendChannelInitializer}
 */
public class BackendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcInvokerDef invokerDef;

    public BackendChannelInitializer(RpcInvokerDef invokerDef) {
        this.invokerDef = invokerDef;
    }

    /**
     * temporarily hard coded for experiment.
     * TODO: init different pipeline according to the remote call.
     */
    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof List) {
                    System.out.println("message arrive backend handler.");
                }
            }
        });
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                System.out.println("Channel active.");
            }
        });

    }
}
