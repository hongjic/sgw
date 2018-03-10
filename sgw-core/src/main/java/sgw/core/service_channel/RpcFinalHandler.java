package sgw.core.service_channel;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
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
    private ThriftChannelContext thriftCtx;

    public RpcFinalHandler(ThriftChannelContext thriftCtx) {
        this.invoker = thriftCtx.getRpcInvoker();
        this.thriftCtx = thriftCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ThriftCallWrapper) {
            logger.debug("Request {}: Sending decoded thrift response back to Http channel pipeline.",
                    thriftCtx.getHttpRequestId());
            invoker.setState(RpcInvoker.InvokerState.SUCCESS);
            writeBackToHttpChannel(msg);
            if (ctx.channel().isActive())
                ctx.close();
        }
        else {
            logger.debug("Request {}: Unrecognized RPC result type. RPC channel closed.",
                    thriftCtx.getHttpRequestId());
            invoker.setState(RpcInvoker.InvokerState.FAIL);
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * Put thrift channel context back into http context
     * @param result
     */
    private void writeBackToHttpChannel(Object result)  {
        invoker.handleResult(result, thriftCtx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (invoker.getState() == RpcInvoker.InvokerState.SUCCESS) {
            logger.debug("Request {}: Rpc channel closed.", thriftCtx.getHttpRequestId());
        }
        else if (invoker.getState() == RpcInvoker.InvokerState.INVOKED) {
            logger.debug("Request {}: Thrift connection fail.", thriftCtx.getHttpRequestId());
            invoker.setState(RpcInvoker.InvokerState.FAIL);
            writeBackToHttpChannel(null);
        }
        else
            ctx.fireChannelInactive();
    }

}