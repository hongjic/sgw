package sgw.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.codec.Charsets;

import java.util.List;

/**
 * stateless
 */
public class EchoServiceEchoParams implements FullHttpRequestParser {

    @Override
    public Object[] parse(FullHttpRequest request) {
        String content = request.content().toString(Charsets.UTF_8);
        request.release();
        Object[] params = new Object[1];
        params[0] = content;
        return params;
    }
}
