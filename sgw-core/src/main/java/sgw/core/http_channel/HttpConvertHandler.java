package sgw.core.http_channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.FastResponseGenerator;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.http_channel.util.ChannelOrderedHttpRequest;
import sgw.core.http_channel.util.ChannelOrderedHttpResponse;
import sgw.core.service_channel.thrift.ThriftCallWrapper;
import sgw.core.service_channel.thrift.ThriftInvokerDef;
import sgw.core.service_channel.thrift.ThriftResultWrapper;
import sgw.core.util.ChannelOrderedMessage;
import sgw.core.util.FastMessage;

import java.util.List;

public class HttpConvertHandler extends MessageToMessageCodec<ChannelOrderedHttpRequest, ChannelOrderedMessage> {

    private final Logger logger = LoggerFactory.getLogger(HttpConvertHandler.class);

    private HttpChannelContext chanCtx;
    private long channelRequestId;

    public HttpConvertHandler(HttpChannelContext chanCtx) {
        this.chanCtx = chanCtx;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ChannelOrderedHttpRequest request, List<Object> out) throws Exception {

        channelRequestId = request.channelMessageId();
        out.add(convertHttptoThrift(request));
    }

    @Override
    public void encode(ChannelHandlerContext ctx, ChannelOrderedMessage responsable, List<Object> out) throws Exception {

        channelRequestId = responsable.channelMessageId();
        if (responsable instanceof FastMessage)
            out.add(convertFastMessagetoHttp(ctx, (FastMessage) responsable));
        if (responsable instanceof ThriftResultWrapper)
            out.add(convertThriftResulttoHttp(ctx, (ThriftResultWrapper) responsable));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        HttpRequestContext reqCtx = chanCtx.getRequestContext(channelRequestId);
        if (cause instanceof DecoderException) {
            cause.printStackTrace();
            new FastMessage(channelRequestId, (DecoderException) cause).send(ctx.channel(), reqCtx);
        }
        if (cause instanceof EncoderException && !reqCtx.getSendFastMessage()) {
            cause.printStackTrace();
            new FastMessage(channelRequestId, (EncoderException) cause).send(ctx.channel(), reqCtx);
        }
        else
            ctx.fireExceptionCaught(cause);
    }

    /**
     * Convert fast message to http response.
     * @param ctx
     * @param message a Fast Message
     * @return generated http response
     * @throws Exception
     */
    private ChannelOrderedHttpResponse convertFastMessagetoHttp(ChannelHandlerContext ctx, FastMessage message) throws Exception {
        HttpRequestContext reqCtx = chanCtx.getRequestContext(channelRequestId);
        FullHttpResponseGenerator generator = new FastResponseGenerator();
        ByteBuf buf = ctx.alloc().ioBuffer();
        Object[] arr = new Object[] {message};
        logger.debug("Request {}: Converting FastMessage to Http response.", reqCtx.getGlobalRequestId());
        FullHttpResponse response;
        try {
            response = generator.generate(arr, buf);
        } catch (Exception e) {
            e.printStackTrace();
            int refCnt = ReferenceCountUtil.refCnt(buf);
            ReferenceCountUtil.release(buf, refCnt);
            throw e;
        }
        return new ChannelOrderedHttpResponse(channelRequestId, response);
    }

    /**
     * Convert thrift result to http response
     * @param ctx
     * @param wrapper A wrapper contains thrift invoke result.
     * @return generated Http Response
     */
    private ChannelOrderedHttpResponse convertThriftResulttoHttp(ChannelHandlerContext ctx, ThriftResultWrapper wrapper)
            throws Exception {
        HttpRequestContext reqCtx = chanCtx.getRequestContext(channelRequestId);
        if (wrapper.getException() != null)
            throw wrapper.getException();
        TBase result = wrapper.getResult();
        FullHttpResponseGenerator responseGenerator = reqCtx.getHttpResponseGenerator();
        logger.debug("Request {}: Converting thrift response to Http response by {}",
                reqCtx.getGlobalRequestId(), responseGenerator.getClass().getName());
        // get size
        int size = 0;
        while (result.fieldForId(size) != null) size ++;
        // construct array
        Object[] arr = new Object[size];
        while (size > 0) {
            TFieldIdEnum field = result.fieldForId(-- size);
            arr[size] = result.getFieldValue(field);
        }
        ByteBuf buf = ctx.alloc().ioBuffer();
        FullHttpResponse response;
        try {
            response = responseGenerator.generate(arr, buf);
        } catch (Exception e) {
            e.printStackTrace();
            int refCnt = ReferenceCountUtil.refCnt(buf);
            ReferenceCountUtil.release(buf, refCnt);
            throw e;
        }
        return new ChannelOrderedHttpResponse(channelRequestId, response);
    }

    /**
     * Convert Http request to thrift
     * @param request http request
     * @return generaated thrift request
     * @throws Exception
     */
    private ThriftCallWrapper convertHttptoThrift(ChannelOrderedHttpRequest request) throws Exception {
        long chReqId = request.channelMessageId();
        HttpRequestContext reqCtx = chanCtx.getRequestContext(chReqId);
        FullHttpRequestParser requestParser = reqCtx.getHttpRequestParser();
        logger.debug("Request {}: Converting Http request by {}",
                reqCtx.getGlobalRequestId(), requestParser.getClass().getName());
        ThriftInvokerDef invokerDef = (ThriftInvokerDef) reqCtx.getInvokerDef();
        Object[] params;
        try {
            params = requestParser.parse(request);
        } catch (Exception e) {
            e.printStackTrace();
            int refCnt = ReferenceCountUtil.refCnt(request);
            ReferenceCountUtil.release(request, refCnt);
            throw e;
        }

        return new ThriftCallWrapper(
                reqCtx,
                createThriftArg(params, invokerDef),
                invokerDef.getServiceName(),
                invokerDef.getMethodName()
        );
    }

    private TBase createThriftArg(Object[] params, ThriftInvokerDef invokerDef) throws Exception {
        TBase args;

        Class<? extends TBase> clazz = invokerDef.getThriftArgsClazz();
        args = clazz.newInstance();

        for (int fieldId = 1; fieldId <= params.length; fieldId++) {
            TFieldIdEnum field = args.fieldForId(fieldId);
            args.setFieldValue(field, params[fieldId - 1]);
        }
        return args;
    }

    private TBase createEmptyThriftResult(ThriftInvokerDef invokerDef) throws Exception {
        TBase result;

        Class<? extends TBase> clazz = invokerDef.getThriftResultClazz();
        result = clazz.newInstance();
        return result;
    }
}
