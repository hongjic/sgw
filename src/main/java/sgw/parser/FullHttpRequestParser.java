package sgw.parser;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * stateless data parser
 */
public interface FullHttpRequestParser {

    Object[] parse(FullHttpRequest request);

}
