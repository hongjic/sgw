package demo.parser;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.FullHttpResponseGenerator;

/**
 * stateless
 */
public class EchoRspGen implements FullHttpResponseGenerator {

    public EchoRspGen() {}

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        String result = (String) results[0];
        buf.writeCharSequence(result, CharsetUtil.UTF_8);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
    }

}
