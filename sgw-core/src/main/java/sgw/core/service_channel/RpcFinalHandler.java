package sgw.core.service_channel;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.thrift.ThriftCallWrapper;
import sgw.core.service_channel.thrift.ThriftChannelContext;

/**
 * Handles all rpc situations, e.g. SUCCESS, FAIL, TIMEOUT.
 * Set {@link sgw.core.service_channel.RpcInvoker.InvokerState} and send back response to
 * http channel.
 */
public class RpcFinalHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(RpcFinalHandler.class);

    private RpcInvoker invoker;

    public RpcFinalHandler(ThriftChannelContext thriftCtx) {
        this.invoker = thriftCtx.getInvoker();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ThriftCallWrapper) {
            logger.debug("Sending decoded thrift response back to Http channel pipeline.");
            invoker.setState(RpcInvoker.InvokerState.SUCCESS);
            writeBackToHttpChannel(msg);

            ctx.close();
        }
        else {
            logger.debug("Unrecognized RPC result type. RPC channel closed.");
            invoker.setState(RpcInvoker.InvokerState.FAIL);
            ctx.close();
        }
    }

    private void writeBackToHttpChannel(Object result)  {
        invoker.handleResult(result);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (invoker.getState() == RpcInvoker.InvokerState.INVOKED) {
            logger.debug("Thrift connection lose.");
            invoker.setState(RpcInvoker.InvokerState.FAIL);
            writeBackToHttpChannel(null);
        }
        ctx.fireChannelInactive();
    }

}