package sgw.core.service_channel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.thrift.TBase;
import sgw.core.service_channel.thrift.*;
import sgw.core.util.ChannelOrderedMessage;

public class ServiceHandler extends ChannelDuplexHandler {

    private ThriftChannelContext chanCtx;

    public ServiceHandler(ThriftChannelContext chanCtx) {
        this.chanCtx = chanCtx;
    }

    /**
     * receives {@link ThriftCallWrapper}, send {@link ThriftOrderedRequest} to next handler
     * @param ctx
     * @param msg
     * @param promise
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        assert msg instanceof ChannelOrderedMessage;

        if (msg instanceof ThriftCallWrapper) {
            ThriftCallWrapper call = (ThriftCallWrapper) msg;
            ThriftRequestContext tReqCtx = chanCtx.newRequestContext(call.getHttpReqCtx());
            ThriftOrderedRequest request = new ThriftOrderedRequest();
            request.setArgs(call.getArgs());
            request.setServiceName(call.getServiceName());
            request.setMethodName(call.getMethodName());
            request.setChannelRequestId(tReqCtx.getRpcChRequestId());
            tReqCtx.setRequest(request);
            ctx.write(request, promise);
        }

    }

    /**
     * receives {@link ThriftOrderedResponse}, send {@link ThriftResultWrapper} back to http channel.
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        assert msg instanceof ThriftOrderedResponse;

        ThriftOrderedResponse response = (ThriftOrderedResponse) msg;
        long rpcChReqId = response.channelMessageId();
        TBase tresult = response.getResult();

        ThriftRequestContext tReqCtx = chanCtx.getRequestContext(rpcChReqId);
        chanCtx.removeRequestContext(rpcChReqId);
        RpcInvoker invoker = tReqCtx.getRpcInvoker();
        long httpChReqId = tReqCtx.getHttpChRequestId();
        ThriftResultWrapper result = new ThriftResultWrapper(httpChReqId, tresult);
        invoker.setState(RpcInvoker.InvokerState.SUCCESS);
        invoker.handleResult(result);
    }
}
