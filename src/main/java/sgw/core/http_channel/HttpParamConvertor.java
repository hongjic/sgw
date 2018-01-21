package sgw.core.http_channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.parser.FullHttpRequestParser;

import java.util.List;

public class HttpParamConvertor extends MessageToMessageDecoder<FullHttpRequest>{

    private final Logger logger = LoggerFactory.getLogger(HttpParamConvertor.class);
    private static final String FORMAT = "examples.thrift_service.%s.%s_args";

    private HttpChannelContext httpCtx;

    public HttpParamConvertor(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        // FullHttpRequestParser has been created before this.
        FullHttpRequestParser parser = httpCtx.getFullHttpRequestParser();
        logger.info("Start decoding FullHttpRequest using {}", parser.getClass().getName());
        Object[] params = parser.parse(request);

        RpcInvokerDef invokerDef = httpCtx.getInvoker().getInvokerDef();
        String serviceName = invokerDef.getServiceName();
        String methodName = invokerDef.getMethodName();

        TBase<?, TFieldIdEnum> args = createThriftArg(params, serviceName, methodName);
        out.add(args);
    }

    private TBase<?, TFieldIdEnum> createThriftArg(Object[] params, String serviceName, String methodName) throws Exception {
        TBase<?, TFieldIdEnum> args = null;
        try {
            Class clazz = Class.forName(String.format(FORMAT, serviceName, methodName));
            args = (TBase<?, TFieldIdEnum>) clazz.newInstance();

            for (int fieldId = 1; fieldId <= params.length; fieldId++)
                args.setFieldValue(args.fieldForId(fieldId), params[fieldId]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("Thrift class named as {} can not be found.", serviceName);
        }
        return args;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
