package demo.parser;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.DupleHttpConvertor;

public class EchoConvertor implements DupleHttpConvertor {

    @Override
    public Object[] parse(FullHttpRequest request) {
        Object[] params = new Object[1];
        params[0] = request.content().toString(CharsetUtil.UTF_8);
        return params;
    }

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        String result = (String) results[0];
        buf.writeCharSequence(result, CharsetUtil.UTF_8);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
    }
}
