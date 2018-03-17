package sgw.core.http_channel;

import io.netty.channel.*;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.thrift.ThriftCallWrapper;
import sgw.core.service_channel.thrift.ThriftResultWrapper;
import sgw.core.util.FastMessage;

public class InvokeHandler extends ChannelInboundHandlerAdapter {

    private HttpChannelContext chanCtx;
    private final EventLoop httpEventLoop;

    public InvokeHandler(HttpChannelContext chanCtx) {
        this.chanCtx = chanCtx;
        this.httpEventLoop = chanCtx.getHttpChannel().eventLoop();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        /**
         * msg type: {@link sgw.core.util.ChannelOrderedMessage}
         * currently only support thrift, so msgs are of type {@link ThriftCallWrapper}
         */
        assert msg instanceof ThriftCallWrapper;
        ThriftCallWrapper treq = (ThriftCallWrapper) msg;
        long chReqId = treq.channelMessageId();
        HttpRequestContext reqCtx = chanCtx.getRequestContext(chReqId);
        final RpcInvoker invoker = reqCtx.getInvoker();
        invoker.invokeAsync(treq).addListener((ChannelFuture f) -> {
            if (httpEventLoop.inEventLoop())
                ifInvokeSuccess(ctx, f, invoker, reqCtx);
            else
                httpEventLoop.execute(() -> ifInvokeSuccess(ctx, f, invoker, reqCtx));
        });
    }

    private void ifInvokeSuccess(ChannelHandlerContext ctx, ChannelFuture future, RpcInvoker invoker, HttpRequestContext reqCtx) {
        assert httpEventLoop.inEventLoop();

        if (future.isSuccess())
            invoker.setState(RpcInvoker.InvokerState.INVOKED);
        else {
            invoker.setState(RpcInvoker.InvokerState.INVOKE_FAIL);
            futureFailForCause(ctx, future.cause(), reqCtx);
        }

    }

    private void futureFailForCause(ChannelHandlerContext ctx, Throwable cause, HttpRequestContext reqCtx) {
        cause.printStackTrace();
        if (cause instanceof Exception) {
            long chReqId = reqCtx.getChannelRequestId();
            FastMessage fm = new FastMessage(chReqId, (Exception) cause);
            fm.send(ctx, reqCtx);
        }
        else {
            ctx.close();
        }
    }

}
