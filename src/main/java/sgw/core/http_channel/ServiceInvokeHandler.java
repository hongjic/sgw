package sgw.core.http_channel;

import io.netty.channel.*;
import sgw.NettyGatewayServerConfig;
import sgw.ThreadPoolStrategy;
import sgw.core.service_channel.RpcInvoker;

public class ServiceInvokeHandler extends ChannelInboundHandlerAdapter {

    /**
     * When using Thrift, it is a TBase object representing the Thrift request
     */
    private Object invokeParam;
    private HttpChannelContext httpCtx;

    public ServiceInvokeHandler(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        httpCtx.setInvokeParam(invokeParam = msg);

        Channel inBoundChannel = ctx.channel();
        RpcInvoker invoker = httpCtx.getInvoker();
        if (invoker != null) {
            connectAndInvoke(invoker, inBoundChannel);
        }
    }

    /**
     * @param currentChannel the http channel
     */
    public void connectAndInvoke(RpcInvoker invoker, final Channel currentChannel) {
        ThreadPoolStrategy tpStrategy = NettyGatewayServerConfig.getCurrentConfig().getThreadPoolStrategy();
        invoker.setInboundChannel(currentChannel);
        // register the rpc channel to a eventloop group and connect
        if (tpStrategy.isMultiWorkers()) {
            /**
             * Because http channels and rpc channels share the same thread pool,
             * register the rpc channels to the same thread as the Http channel
             * to reduce potential context switching.
             */
            invoker.register(currentChannel.eventLoop());
        } else {
            invoker.register(tpStrategy.getBackendGroup());
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
            currentChannel.close();
        }
    }

    private void doInvoke(RpcInvoker invoker) {
        if (invoker.getState() == RpcInvoker.InvokerState.ACTIVE && invokeParam != null) {
            try {
                invoker.invokeAsync(invokeParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
