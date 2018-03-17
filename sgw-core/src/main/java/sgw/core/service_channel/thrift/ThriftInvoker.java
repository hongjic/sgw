package sgw.core.service_channel.thrift;

import io.netty.channel.*;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.pool.ChannelPoolMngr;
import sgw.core.service_channel.*;
import sgw.core.service_discovery.ServiceNode;
import sgw.core.util.ChannelOrderedMessage;

/**
 * has to be thread-safe
 *
 */
public class ThriftInvoker extends AbstractRpcInvoker<ThriftCallWrapper, ThriftResultWrapper> {

    private Logger logger = LoggerFactory.getLogger(ThriftInvoker.class);

    private Channel thriftChannel;
    private EventLoop httpEventLoop;

    public ThriftInvoker(RpcInvokerDef invokerDef,
                         ServiceNode serviceNode,
                         HttpChannelContext chanCtx,
                         HttpRequestContext reqCtx) {
        super(invokerDef, serviceNode, chanCtx, reqCtx);
        this.httpEventLoop = chanCtx.getHttpChannel().eventLoop();
    }

    @Override
    public ChannelFuture invokeAsync(ThriftCallWrapper param) {
        ChannelPool pool = ChannelPoolMngr.Instance.pool(serviceNode);
        setState(InvokerState.CONNECTING);
        Future<Channel> future = pool.acquire();
        Channel httpChannel = httpChanCtx.getHttpChannel();
        ChannelPromise promise = httpChannel.pipeline().newPromise();
        if (future.isDone()) {
            notifyChannelAcquired(future, promise, param);
            if (!promise.isSuccess())
                return promise;
        }
        else {
            final EventLoop eventLoop = httpEventLoop;
            future.addListener((Future<Channel> future1) -> {
                if (eventLoop.inEventLoop())
                    notifyChannelAcquired(future1, promise, param);
                else
                    eventLoop.execute(() -> notifyChannelAcquired(future1, promise, param));
            });
        }

        return promise;
    }

    private void notifyChannelAcquired(Future<Channel> future, ChannelPromise promise, Object param) {
        assert httpEventLoop.inEventLoop();
        if (future.isSuccess()) {
            thriftChannel = future.getNow();
            logger.debug("Connection established: " + invokerDef.toString());
            setState(InvokerState.ACTIVE);
            doInvoke(thriftChannel, param, promise);
        }
        else {
            logger.debug("Connection failure: " + invokerDef.toString());
            setState(InvokerState.CONNECT_FAIL);
            promise.setFailure(future.cause());
        }
    }

    private ChannelFuture doInvoke(Channel rpcChannel, Object param, ChannelPromise promise) {
        final EventLoop eventLoop = rpcChannel.eventLoop();
        if (eventLoop.inEventLoop())
            doInvoke0(rpcChannel, param, promise);
        else
            eventLoop.execute(() -> doInvoke0(rpcChannel, param, promise));
        return promise;
    }

    private void doInvoke0(Channel rpcChannel, Object param, ChannelPromise promise) {
        assert rpcChannel.eventLoop().inEventLoop();
        rpcChannel.writeAndFlush(param).addListener((future) -> {
            if (future.isSuccess())
                promise.setSuccess();
            else
                promise.setFailure(future.cause());
        });
    }

    @Override
    public void handleResult(ThriftResultWrapper tResult) {
        tResult.channelMessageId();
        final EventLoop eventLoop = httpEventLoop;
        if (eventLoop.inEventLoop())
            handleResult0(tResult);
        else
            eventLoop.execute(() -> handleResult0(tResult));
    }

    private void handleResult0(ThriftResultWrapper result) {
        assert httpEventLoop.inEventLoop();
        Channel httpChannel = this.httpChanCtx.getHttpChannel();
        httpChannel.writeAndFlush(result);
    }

}
