package sgw.core.service_channel;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.thrift.ThriftCallWrapper;
import sgw.core.service_channel.thrift.ThriftChannelContext;

public class RpcFinalHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(RpcFinalHandler.class);

    private RpcInvoker invoker;

    public RpcFinalHandler(ThriftChannelContext thriftCtx) {
        this.invoker = thriftCtx.getInvoker();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ThriftCallWrapper) {
            logger.info("Sending decoded thrift response back to Http channel pipeline.");
            writeBackToHttpChannel(msg);
            ctx.close();
        }
        else {
            logger.info("Unrecognized RPC result type. RPC channel closed.");
            ctx.close();
        }
    }

    private void writeBackToHttpChannel(Object msg)  {
        invoker.receiveResult(msg);
    }

}
