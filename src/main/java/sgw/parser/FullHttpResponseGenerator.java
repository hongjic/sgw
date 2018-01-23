package sgw.parser;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * stateless
 */
public interface FullHttpResponseGenerator {

    /**
     *
     * @param results array of object represents the decoded thrift result
     *                sometimes it may return a tuple, so array is necessary.
     * @param buf allocated ByteBuf instance to write http content in.
     * @return a FullHttpResponse
     */
    FullHttpResponse generate(Object[] results, ByteBuf buf);

}
