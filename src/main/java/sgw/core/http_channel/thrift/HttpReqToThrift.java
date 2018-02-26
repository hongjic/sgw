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
        // FullHttpRequestParser has been created before this.
        FullHttpRequestParser requestParser = httpCtx.getFullHttpRequestParser();
        logger.info("Converting Http request to Thrift request BY {}", requestParser.getClass().getName());
        // parse http request into an array of parameters.
        Object[] params = requestParser.parse(request);

        ThriftInvokerDef invokerDef = (ThriftInvokerDef) httpCtx.getInvokerDef();

        TBase<?, TFieldIdEnum> args = createThriftArg(params, invokerDef);
        TBase result = createEmptyThriftResult(invokerDef);
        TMessage message = new TMessage(invokerDef.getMethodName(), TMessageType.CALL, 0);
        String serviceName = invokerDef.getServiceName().toLowerCase();
        ThriftCallWrapper wrapper = new ThriftCallWrapper(args, result, message, serviceName);
        out.add(wrapper);
    }

    private TBase<?, TFieldIdEnum> createThriftArg(Object[] params, ThriftInvokerDef invokerDef) throws Exception {
        TBase<?, TFieldIdEnum> args;

        try {
            Class<?> clazz = invokerDef.getThriftArgsClazz();
            args = (TBase<?, TFieldIdEnum>) clazz.newInstance();

            for (int fieldId = 1; fieldId <= params.length; fieldId++) {
                TFieldIdEnum field = args.fieldForId(fieldId);
                args.setFieldValue(field, params[fieldId - 1]);
            }
        } catch (ClassNotFoundException e) {
            // Deal wiht ClassNotFoundException separately here. Later all Exceptions will be
            // converted into DecoderException.
            logger.error("Thrift class named as {} can not be found.",
                    invokerDef.getThriftArgsClazzName());
            throw e;
        }
        return args;
    }

    private TBase createEmptyThriftResult(ThriftInvokerDef invokerDef) throws Exception {
        TBase result;

        try {
            Class<?> clazz = invokerDef.getThriftResultClazz();
            result = (TBase) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Thrift class named as {} can not be found.",
                    invokerDef.getThriftResultClazzName());
            throw e;
        }
        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof DecoderException) {
            cause.printStackTrace();
            ChannelFuture future = FastMessageSender.send(ctx, new FastMessage((DecoderException) cause));
            future.addListener(ChannelFutureListener.CLOSE);
        }
        else
            ctx.fireExceptionCaught(cause);
    }
}
