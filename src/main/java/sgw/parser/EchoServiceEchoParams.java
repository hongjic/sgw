package sgw.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.codec.Charsets;

/**
 * stateless
 */
public class EchoServiceEchoParams implements FullHttpRequestParser {

    public EchoServiceEchoParams() {}

    @Override
    public Object[] parse(FullHttpRequest request) {
        Object[] params = new Object[1];
        params[0] = request.content().toString(Charsets.UTF_8);
        return params;
    }
}
