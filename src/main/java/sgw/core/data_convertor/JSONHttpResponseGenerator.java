package sgw.core.data_convertor;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public abstract class JSONHttpResponseGenerator implements FullHttpResponseGenerator {

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        JSON json = generateJSONBody(results);
        String body = json.toJSONString();
        buf.writeCharSequence(body, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    abstract protected JSON generateJSONBody(Object[] results);
}
