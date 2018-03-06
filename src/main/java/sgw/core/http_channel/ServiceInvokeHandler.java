package sgw.core.http_channel;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.NettyGatewayServerConfig;
import sgw.ThreadPoolStrategy;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.util.FastMessage;
import sgw.core.util.FastMessageSender;

import javax.swing.event.ChangeListener;

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
        // invokeResult is ready
        RpcInvoker invoker = httpCtx.getInvoker();
        switch (invoker.getState()) {
            case SUCCESS:
                handleResultSuccess(ctx, invokeResult);
                break;
            case FAIL:
                handleResultFail(ctx);
                break;
            case TIMEOUT:
                handleResultTimeout(ctx);
        }
    }

    private void handleResultSuccess(final ChannelHandlerContext ctx, Object invokeResult) {
        // invokeResult is not null
        ChannelFuture future = ctx.channel().writeAndFlush(invokeResult);
        final ChannelHandlerContext context = ctx;
        future.addListener((ChannelFuture writeFuture) -> {
            if (writeFuture.isSuccess()) {
                context.channel().close();
            }
            else {
                futureFailForCause(context, writeFuture.cause());
            }
        });
    }

    private void handleResultFail(final ChannelHandlerContext ctx) {
        httpCtx.setSendFastMessage(true);
        FastMessage message = new FastMessage("Downstream connection lose.");
        message.setHttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        FastMessageSender.send(ctx, message).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleResultTimeout(final ChannelHandlerContext ctx) {
        httpCtx.setSendFastMessage(true);
        FastMessage message = new FastMessage("Downstream service timeout.");
        message.setHttpResponseStatus(HttpResponseStatus.REQUEST_TIMEOUT);
        FastMessageSender.send(ctx, message).addListener(ChannelFutureListener.CLOSE);
    }

    private void futureFailForCause(ChannelHandlerContext ctx, Throwable cause) {
        futureFailForCause(ctx.channel(), cause);
    }

    /**
     * Send back a fast message or just close the channel when future fails.
     * @param channel the channel to operate on
     * @param cause the cause of future's failure
     */
    private void futureFailForCause(Channel channel, Throwable cause) {
        if (cause instanceof Exception) {
            httpCtx.setSendFastMessage(true);
            ChannelFuture f = FastMessageSender.send(channel, new FastMessage((Exception) cause));
            f.addListener(ChannelFutureListener.CLOSE);
        }
        else {
            channel.close();
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
            // send back fast message and close channel if the backend connections fails.
            futureFailForCause(currentChannel, future.cause());
        }
    }

    private void doInvoke(RpcInvoker invoker) {
        if (invoker.getState() == RpcInvoker.InvokerState.ACTIVE && invokeParam != null) {
            logger.info("Sending message to RPC channel pipeline,");
            invoker.invokeAsync(invokeParam)
                    .addListener((x) -> logger.info("Request sent to RPC channel."));
        }
    }

}
