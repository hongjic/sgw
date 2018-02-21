package sgw.core.http_channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.Convertors;
import sgw.core.data_convertor.FullHttpResponseGenerator;
import sgw.core.filters.FastMessage;

import java.util.List;

public class FastMessageToHttpRsp extends MessageToMessageEncoder<FastMessage> {

    private final Logger logger = LoggerFactory.getLogger(FastMessageToHttpRsp.class);

    @Override
    public void encode(ChannelHandlerContext ctx, FastMessage message, List<Object> out) {
        String clazzName = Convertors.FAST_RESPONSE_GENERATOR;
        FullHttpResponseGenerator generator = Convertors.Cache.getResGen(clazzName);
        logger.info("Converting FastMessage to Http response BY {}",
                generator.getClass().getName());

        ByteBuf buf = ctx.alloc().ioBuffer();
        Object[] arr = new Object[1];
        arr[0] = message;
        FullHttpResponse response = generator.generate(arr, buf);
        out.add(response);
    }
}
