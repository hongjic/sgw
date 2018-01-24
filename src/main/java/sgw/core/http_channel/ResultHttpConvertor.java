package sgw.core.http_channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.FullHttpResponseGenerator;

import java.util.List;

public class ResultHttpConvertor extends MessageToMessageEncoder<TBase> {

    private final Logger logger = LoggerFactory.getLogger(ResultHttpConvertor.class);

    private HttpChannelContext httpCtx;

    public ResultHttpConvertor(HttpChannelContext httpCtx) {
        this.httpCtx = httpCtx;
    }

    /**
     *
     * @param out only return a http response
     */
    @Override
    public void encode(ChannelHandlerContext ctx, TBase result, List<Object> out) {
        FullHttpResponseGenerator responseGenerator = httpCtx.getResponseGenerator();
        logger.info("Converting Thrift response to Http response BY {}",
                responseGenerator.getClass().getName());
        // get size
        int size = 0;
        while (result.fieldForId(size) != null) {
            size ++;
        }
        // construct array
        Object[] arr = new Object[size];
        while (size > 0) {
            -- size;
            TFieldIdEnum field = result.fieldForId(size);
            arr[size] = result.getFieldValue(field);
        }

        ByteBuf buf = ctx.alloc().ioBuffer();
        FullHttpResponse response = responseGenerator.generate(arr, buf);
        out.add(response);
    }

}