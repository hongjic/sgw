package sgw.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.ThreadPoolStrategy;
import sgw.core.routing.Router;
import sgw.core.services.RpcInvoker;
import sgw.core.services.RpcInvoker.InvokerState;
import sgw.core.services.RpcInvokerDef;
import sgw.core.services.RpcInvokerDetector;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class HttpServerHandler extends ChannelInboundHandlerAdapter{

    private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private HttpPostRequestDecoder postDecoder;
    private boolean readingChunks;
    private Router router;
    private RpcInvokerDetector invokerDetector;
    private ThreadPoolStrategy tpStrategy;
    private RpcInvoker invoker;
    private StringBuilder responseContent = new StringBuilder();

    /**
     * this is a parameter waiting to be converted to a rpc method java param.
     * it will be passed to HttpToParamDecoder handler when things are ready.
     */
    private List<Object> invokeParam;


    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    public void useRouter(Router router) {
        this.router = router;
    }

    public void useInvokerManager(RpcInvokerDetector invokerDetector) {
        this.invokerDetector = invokerDetector;
    }

    public void useThreadPoolStrategy(ThreadPoolStrategy strategy) {
        tpStrategy = strategy;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info(msg.getClass().getName());
        /**
         * msg types:
         * {@link HttpRequest}
         * {@link HttpContent}
         */
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            HttpMethod method = request.method();
            URI uri = new URI(request.uri());

            logger.info("HttpRequest Method : {}", method);
            logger.info("HttpRequest URI: {}", uri.toString());

            // rpc invoker can be determined as soon as we get HttpRequestDef
            // no need to wait for the full request body arrives.
            HttpRequestDef httpRequestDef = new HttpRequestDef(request);
            RpcInvokerDef invokerDef = router.getRpcInvokerDef(httpRequestDef);
            // get remote service
            invoker = invokerDetector.find(invokerDef);

            final Channel inBoundChannel = ctx.channel();
            invoker.setInboundChannel(inBoundChannel);
            // register the rpc channel to a eventloop group and connect
            if (tpStrategy.isMultiWorkers()) {
                /**
                 * Because http channels and rpc channels share the same thread pool,
                 * register the rpc channels to the same thread as the Http channel
                 * to reduce potential context switching.
                 */
                invoker.register(inBoundChannel.eventLoop());
            }
            else {
                invoker.register(tpStrategy.getBackendGroup());
            }
            ChannelFuture future = invoker.connect();

            future.addListener((ChannelFuture future1) -> {
                EventLoop eventloop = inBoundChannel.eventLoop();
                if (eventloop.inEventLoop()) {
                    doInvokeOrNot(future1, inBoundChannel);
                } else {
                    /**
                     * add a task back to the http channel thread for later process.
                     * Gurantee each eventloop has its own responsibility and avoid
                     * potential race condition.
                     */
                    eventloop.execute(() ->
                            doInvokeOrNot(future1, inBoundChannel));
                }
            });

            if (method.equals(HttpMethod.POST)) {
                postDecoder = new HttpPostRequestDecoder(factory, request);
                readingChunks = HttpUtil.isTransferEncodingChunked(request);
            }
        }

        if (msg instanceof HttpContent) {
            if (msg == LastHttpContent.EMPTY_LAST_CONTENT)
                return;

            // New chunk has arrived
            HttpContent chunk = (HttpContent) msg;
            // postDecoder should have been created before
            assert postDecoder != null;
            postDecoder.offer(chunk);

            try {
                while (postDecoder.hasNext()) {
                    InterfaceHttpData data = postDecoder.next();
                    if (data != null) {
                        try {
                            writeHttpData(data);
                        } finally {
                            data.release();
                        }
                    }
                }
            } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {

            }

            // if it is the last chunk, swith `readingChunks`
            // also, write the response.
            if (chunk instanceof LastHttpContent) {
//                writeResponse(ctx.channel());
                /**
                 * ============attention below==========
                 * experiment
                 */
                invokeParam = Arrays.asList("hello world!");
                if (invoker.getState() == InvokerState.ACTIVE) {
                    // connected but not invoked.
                    invoker.invoke(invokeParam);
                }
                readingChunks = false;
                reset();
            }
        }

    }

    /**
     *
     * @param future the ChannelFuture of the listener.
     * @param currentChannel the Channel that owns the current handler.
     */
    private void doInvokeOrNot(ChannelFuture future, Channel currentChannel) {
        if (future.isSuccess()) {
            if (invoker.getState() != InvokerState.INVOKED && invokeParam != null) {
                try {
                    invoker.invoke(invokeParam);
                    System.out.println("Connected in listener2: " + Thread.currentThread().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // close the http connection if the backend connection fails.
            currentChannel.close();
        }
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String name, value;
            try {
                name = attribute.getName();
                value = attribute.getValue();
            } catch (Exception e1) {
                e1.printStackTrace();
                return;
            }
            responseContent.append("\r\nBODY Attribute: " + name + ":"
                    + value + "\r\n");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("Channel read complete.");
        ctx.flush();
    }

    private void reset() {
        postDecoder.destroy();
        postDecoder = null;
    }

    private void writeResponse(Channel channel) {
        ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        // clear state
        responseContent.setLength(0);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}
