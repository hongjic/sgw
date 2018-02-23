package sgw.core.data_convertor;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public abstract class JSONHttpResponseGenerator implements FullHttpResponseGenerator {

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        String body = generateJSONBody(results);
        buf.writeCharSequence(body, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    abstract protected String generateJSONBody(Object[] results);
}
