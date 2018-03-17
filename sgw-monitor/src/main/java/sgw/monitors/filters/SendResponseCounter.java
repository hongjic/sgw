package sgw.monitors.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.filters.AbstractFilter;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.util.FastMessage;
import sgw.monitors.GatewayMonitor;
import sgw.monitors.NumCounter;

public class SendResponseCounter extends AbstractFilter{

    private final Logger logger = LoggerFactory.getLogger(SendResponseCounter.class);

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter(HttpRequestContext httpCtx) {
        return true;
    }

    @Override
    public FastMessage run(HttpRequestContext httpCtx) {
        GatewayMonitor.getInstance().getCounter(GatewayMonitor.SND_RES, NumCounter.class).increase();
//        long id = (long) httpCtx.get("request_id");
//        long now = System.currentTimeMillis();
//        long start = (long) httpCtx.get("request_start_time");
//        if (now - start > 1000) {
//            logger.info("Send response {} at {}, using {}ms", id, now, now - start);
//            long s1 = (long) httpCtx.get("pre_filters_start");
//            long s2 = (long) httpCtx.get("routing_handler_start");
//            long s3 = (long) httpCtx.get("request_convertor_handler_start");
//            long s4 = (long) httpCtx.get("invoke_handler_start");
//            long s5 = httpCtx.getRpcSentTime();
//            long ss = (long) httpCtx.get("$rpc_send_time");
//            long s6 = httpCtx.getRpcRecvTime();
//            long s7 = (long) httpCtx.get("writability_changed_start");
//            long s8 = (long) httpCtx.get("response_convertor_handler_start");
//            long s9 = (long) httpCtx.get("post_filters_start");
//            logger.info("{} {} {} {} {} {} {} {} {}", s2-s1, s3-s2, s4-s3, s5-s4, ss-s5, s6-s5, s7-s6, s8-s7, s9-s8);
//        }
//        if (httpCtx.getHttpResponse().status().code() != 200)
//            logger.info("Response {} return {}.", id, httpCtx.getHttpResponse().status().code());
        return null;
    }

}
