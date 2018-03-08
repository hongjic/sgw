package sgw.core.http_channel.thrift;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.service_channel.thrift.ThriftCallWrapper;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.service_channel.thrift.ThriftInvokerDef;
import sgw.core.util.FastMessage;
import sgw.core.util.FastMessageSender;

import java.util.List;

public class HttpReqToThrift extends MessageToMessageDecoder<FullHttpRequest>{

    private final Logger logger = LoggerFactory.getLogger(HttpReqToThrift.class);

    private HttpChannelContext httpCtx;

    public HttpReqToThrift(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        httpCtx.put("request_convertor_handler_start", System.currentTimeMillis());
        // FullHttpRequestParser has been created before this.
        FullHttpRequestParser requestParser = httpCtx.getFullHttpRequestParser();
        logger.debug("Converting Http request to Thrift request BY {}", requestParser.getClass().getName());
        // parse http request into an array of parameters.
        Object[] params = requestParser.parse(request);

        ThriftInvokerDef invokerDef = (ThriftInvokerDef) httpCtx.getInvokerDef();

        TBase args = createThriftArg(params, invokerDef);
        TBase result = createEmptyThriftResult(invokerDef);
        TMessage message = new TMessage(invokerDef.getMethodName(), TMessageType.CALL, 0);
        String serviceName = invokerDef.getServiceName().toLowerCase();
        ThriftCallWrapper wrapper = new ThriftCallWrapper(args, result, message, serviceName);
        out.add(wrapper);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof DecoderException) {
            cause.printStackTrace();
            httpCtx.setSendFastMessage(true);
            ChannelFuture future = FastMessageSender.send(ctx, new FastMessage((DecoderException) cause));
            future.addListener(ChannelFutureListener.CLOSE);
        }
        else
            ctx.fireExceptionCaught(cause);
    }
}
