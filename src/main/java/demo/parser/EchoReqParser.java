package demo.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.FullHttpRequestParser;

/**
 * stateless
 */
public class EchoReqParser implements FullHttpRequestParser {

    public EchoReqParser() {}

    @Override
    public Object[] parse(FullHttpRequest request) {
        Object[] params = new Object[1];
        params[0] = request.content().toString(CharsetUtil.UTF_8);
        return params;
    }
}
