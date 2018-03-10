package sgw.core.data_convertor;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ResponseGeneratorImpl implements FullHttpResponseGenerator {

    private ConvertorInfo cinfo;

    public ResponseGeneratorImpl(ConvertorInfo cinfo) {
        this.cinfo = cinfo;
    }

    /**
     * Thrift always return only one result. So this method only use the
     * first element in {@code results} to generate http response.
     * May need to upgrade in future.
     *
     * @param results array of object represents the decoded thrift result
     *                sometimes it may return a tuple, so array is necessary.
     * @param buf allocated ByteBuf instance to write http content in.
     * @return
     */
    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) throws Exception {
        Object one = results[0];
        String responseBody = generateBody(one);
        buf.writeCharSequence(responseBody, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseBody.length());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        return response;
    }

    /**
     *
     * @param result thrift return object
     * @return generated http response body
     * @throws Exception
     */
    private String generateBody(Object result) throws Exception {
        Method responseGenerator = cinfo.getResponseGenerator();
        Object convertor = cinfo.getConvertor();
        return (String) responseGenerator.invoke(convertor, result);
    }
}
