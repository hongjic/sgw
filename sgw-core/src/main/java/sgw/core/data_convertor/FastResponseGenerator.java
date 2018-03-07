package sgw.core.data_convertor;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import sgw.core.util.FastMessage;

/**
 * HttpResponse Convertor for {@link FastMessage}
 */
public class FastResponseGenerator implements FullHttpResponseGenerator {

    @Override
    public FullHttpResponse generate(Object[] results, ByteBuf buf) {
        FastMessage message = (FastMessage) results[0];
        HttpResponseStatus status = message.getStatus();
        String body = message.getResponseBody();
        buf.writeCharSequence(body, CharsetUtil.UTF_8);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
    }
}
