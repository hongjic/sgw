package sgw.core.filters.post_routing;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import sgw.core.filters.AbstractFilter;
import sgw.core.http_channel.HttpChannelContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHeader extends AbstractFilter {

    private static final String pattern = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter(HttpChannelContext ctx) {
        return true;
    }

    @Override
    public Object run(HttpChannelContext ctx) {
        FullHttpResponse response = ctx.getHttpResponse();
        response.headers().set(HttpHeaderNames.DATE, getCurrentDate());
        return null;
    }

    private String getCurrentDate() {
        long now = System.currentTimeMillis();
        return format.format(new Date(now));
    }


}
