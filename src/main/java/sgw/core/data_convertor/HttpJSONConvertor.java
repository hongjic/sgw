package sgw.core.data_convertor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public abstract class HttpJSONConvertor implements DupleHttpConvertor {

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        String str = generateJSONResponse(results);
        buf.writeCharSequence(str, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
        return response;
    }

    abstract protected String generateJSONResponse(Object[] results);

    @Override
    public Object[] parse(FullHttpRequest request) {
        String content = request.content().toString(CharsetUtil.UTF_8);
        JSONObject json = JSON.parseObject(content);
        Object[] params = parseJSONRequest(json);
        return params;
    }

    abstract protected Object[] parseJSONRequest(JSONObject json);

}
