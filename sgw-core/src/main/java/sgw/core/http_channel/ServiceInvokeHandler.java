package sgw.core.http_channel;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.NettyGatewayServerConfig;
import sgw.core.ThreadPoolStrategy;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.util.FastMessage;

public class ServiceInvokeHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ServiceInvokeHandler.class);
    /**
     * When using Thrift, it is a ThriftCallWrapper object representing the Thrift request
     */
    private Object invokeParam;
    private HttpChannelContext httpCtx;

    public ServiceInvokeHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        httpCtx.put("invoke_handler_start", System.currentTimeMillis());
        invokeParam = msg;

        RpcInvoker invoker = httpCtx.getInvoker();
        if (invoker != null) {
            connectAndInvoke(invoker, ctx);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        httpCtx.put("writability_changed_start", System.currentTimeMillis());
        RpcInvoker invoker = httpCtx.getInvoker();
        switch (invoker.getState()) {
            case SUCCESS:
                // invokeResult is ready
                handleResultSuccess(ctx);
                break;
            case FAIL:
                handleResultFail(ctx);
                break;
            case TIMEOUT:
                handleResultTimeout(ctx);
                break;
            default:
                throw new Exception("invoker state: " + invoker.getState().name());

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        RpcInvoker invoker;
        Channel rpcChannel;
        if ((invoker = httpCtx.getInvoker()) != null && (rpcChannel = invoker.getRpcChannel()).isActive())
            rpcChannel.close();
        logger.debug("Request {}: Http channel closed.", httpCtx.getRequestId());
    }

    private void handleResultSuccess(final ChannelHandlerContext ctx) {
        // invokeResult is not null
        Channel rpcChannel = httpCtx.getInvoker().getRpcChannel();
        if (rpcChannel.isActive())
            rpcChannel.close();
        Object invokeResult = httpCtx.getInvokeResult();
        ChannelFuture future = ctx.channel().writeAndFlush(invokeResult);
        final ChannelHandlerContext context = ctx;
        future.addListener((ChannelFuture writeFuture) -> {
            if (writeFuture.isSuccess()) {
                context.close();
            }
            else {
                futureFailForCause(context, writeFuture.cause());
            }
        });
    }

    private void handleResultFail(final ChannelHandlerContext ctx) {
        Channel rpcChannel = httpCtx.getInvoker().getRpcChannel();
        if (rpcChannel.isActive())
            rpcChannel.close();
        httpCtx.setSendFastMessage(true);
        FastMessage message = new FastMessage("Downstream service Fail.");
        message.send(ctx, httpCtx).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleResultTimeout(final ChannelHandlerContext ctx) {
        Channel rpcChannel = httpCtx.getInvoker().getRpcChannel();
        if (rpcChannel.isActive())
            rpcChannel.close();
        httpCtx.setSendFastMessage(true);
        FastMessage message = new FastMessage("Downstream service timeout.");
        message.send(ctx, httpCtx).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * @param ctx the current {@link ChannelHandlerContext}
     */
    private void connectAndInvoke(RpcInvoker invoker, final ChannelHandlerContext ctx) {
        Channel currentChannel = ctx.channel();
        ThreadPoolStrategy tpStrategy = NettyGatewayServerConfig.getCurrentConfig().getThreadPoolStrategy();
        invoker.setInboundChannel(currentChannel);
        // register the rpc channel to a eventloop group and connect
        if (tpStrategy.isMultiWorkers()) {
            /**
             * Because http channels and rpc channels share the same thread pool,
             * register the rpc channels to the same thread as the Http channel
             * to reduce potential context switching.
             */
            invoker.register(currentChannel.eventLoop(), httpCtx);
        } else {
            invoker.register(tpStrategy.getBackendGroup(), httpCtx);
        }

        ChannelFuture future = invoker.connectAsync();
        future.addListener((ChannelFuture future1) -> {
            EventLoop eventloop = currentChannel.eventLoop();
            if (eventloop.inEventLoop()) {
                doInvokeOrNot(invoker, future1, ctx);
            } else {
                /**
                 * add a task back to the http channel thread for later process.
                 * Gurantee each eventloop has its own responsibility and avoid
                 * potential race condition.
                 */
                eventloop.execute(() -> doInvokeOrNot(invoker, future1, ctx));
            }
        });
    }

    /**
     * @param future The rpc channel future
     * @param ctx The current {@link ChannelHandlerContext}
     */
    private void doInvokeOrNot(RpcInvoker invoker, ChannelFuture future, ChannelHandlerContext ctx) {
        if (future.isSuccess()) {
            doInvoke(invoker, ctx);
        }
        else {
            future.channel().close();
            // send back fast message and close channel if the backend connections fails.
            futureFailForCause(ctx, future.cause());
        }
    }

    private void doInvoke(RpcInvoker invoker, ChannelHandlerContext ctx) {
        if (invoker.getState() == RpcInvoker.InvokerState.ACTIVE && invokeParam != null) {
            logger.debug("Request {}: Sending message to RPC channel pipeline.", httpCtx.getRequestId());
            ChannelFuture future = invoker.invokeAsync(invokeParam);
            future.addListener((ChannelFuture f) -> {
                if (f.isSuccess()) {
                    httpCtx.put("$rpc_send_time", System.currentTimeMillis());
                    f.channel().read();
                }
                else {
                    if (f.channel().isActive())
                        f.channel().close();
                    futureFailForCause(ctx, f.cause());
                }
            });
        }
    }

    /**
     * Send back a fast message or just close the channel when future fails.
     * @param ctx the http ctx to send back response or close.
     * @param cause the cause of future's failure
     */
    private void futureFailForCause(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (cause instanceof Exception) {
            FastMessage fm = new FastMessage((Exception) cause);
            fm.send(ctx, httpCtx).addListener(ChannelFutureListener.CLOSE);
        }
        else {
            ctx.close();
        }
    }

}
