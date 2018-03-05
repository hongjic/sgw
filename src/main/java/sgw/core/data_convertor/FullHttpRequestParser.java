package sgw.core.data_convertor;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * stateless data parser
 */
public interface FullHttpRequestParser {

    /**
     *
     * @param request FullHttpRequest
     * @return A Object array represents the parameters included in thrift method call.
     */
    Object[] parse(FullHttpRequest request) throws Exception;

}
