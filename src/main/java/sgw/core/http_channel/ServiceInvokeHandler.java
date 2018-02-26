package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.EncoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.NettyGatewayServerConfig;
import sgw.ThreadPoolStrategy;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_discovery.ServiceUnavailableException;
import sgw.core.util.FastMessage;
import sgw.core.util.FastMessageSender;

public class ServiceInvokeHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ServiceInvokeHandler.class);
    /**
     * When using Thrift, it is a ThriftCallWrapper object representing the Thrift request
     */
    private Object invokeParam;
    private Object invokeResult;
    private HttpChannelContext httpCtx;

    public ServiceInvokeHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        invokeParam = msg;

        Channel inBoundChannel = ctx.channel();
        RpcInvoker invoker = httpCtx.getInvoker();
        if (invoker != null) {
            connectAndInvoke(invoker, inBoundChannel);
        }
    }

    public void receiveResult(Object result) {
        this.invokeResult = result;
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        RpcInvoker invoker = httpCtx.getInvoker();
        if (invoker.getState() == RpcInvoker.InvokerState.FINISHED && invokeResult != null){
            ChannelFuture future = ctx.channel().writeAndFlush(invokeResult);
            final ChannelHandlerContext context = ctx;
            future.addListener((ChannelFuture f1) -> {
                if (f1.isSuccess()) {
                    context.close();
                }
                else {
                    context.pipeline().fireExceptionCaught(f1.cause());
                }
            });
        }

    }

    /**
     * @param currentChannel the http channel
     */
    private void connectAndInvoke(RpcInvoker invoker, final Channel currentChannel) {
        ThreadPoolStrategy tpStrategy = NettyGatewayServerConfig.getCurrentConfig().getThreadPoolStrategy();
        invoker.setInboundChannel(currentChannel);
        // register the rpc channel to a eventloop group and connect
        if (tpStrategy.isMultiWorkers()) {
            /**
             * Because http channels and rpc channels share the same thread pool,
             * register the rpc channels to the same thread as the Http channel
             * to reduce potential context switching.
             */
            invoker.register(currentChannel.eventLoop(), this);
        } else {
            invoker.register(tpStrategy.getBackendGroup(), this);
        }

        ChannelFuture future = invoker.connectAsync();
        future.addListener((ChannelFuture future1) -> {
            EventLoop eventloop = currentChannel.eventLoop();
            if (eventloop.inEventLoop()) {
                doInvokeOrNot(invoker, future1, currentChannel);
            } else {
                /**
                 * add a task back to the http channel thread for later process.
                 * Gurantee each eventloop has its own responsibility and avoid
                 * potential race condition.
                 */
                eventloop.execute(() -> doInvokeOrNot(invoker, future1, currentChannel));
            }
        });
    }

    /**
     * @param future the ChannelFuture of the listener.
     * @param currentChannel the Channel that owns the current handler.
     */
    private void doInvokeOrNot(RpcInvoker invoker, ChannelFuture future, Channel currentChannel) {
        if (future.isSuccess()) {
            doInvoke(invoker);
        }
        else {
            // close the http connection if the backend connections fails.
            // TODO: record failue and send back fast response.
            currentChannel.close();
        }
    }

    private void doInvoke(RpcInvoker invoker) {
        if (invoker.getState() == RpcInvoker.InvokerState.ACTIVE && invokeParam != null) {
            logger.info("Sending message to RPC channel pipeline,");
            invoker.invokeAsync(invokeParam)
                    .addListener((x) -> logger.info("Thrift call sent to downstream service."));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof EncoderException) {
            cause.printStackTrace();
            ChannelFuture future = FastMessageSender.send(ctx, new FastMessage((EncoderException) cause));
            future.addListener(ChannelFutureListener.CLOSE);
        }
        else {
            ctx.fireExceptionCaught(cause);
        }
    }

}
