package sgw.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.FullHttpRequestParser;

/**
 * stateless
 */
public class EchoServiceEchoParams implements FullHttpRequestParser {

    public EchoServiceEchoParams() {}

    @Override
    public Object[] parse(FullHttpRequest request) {
        Object[] params = new Object[1];
        params[0] = request.content().toString(CharsetUtil.UTF_8);
        return params;
    }
}
