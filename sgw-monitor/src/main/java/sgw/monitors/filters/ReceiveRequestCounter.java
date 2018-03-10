package sgw.monitors.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.filters.AbstractFilter;
import sgw.core.http_channel.HttpChannelContext;
import sgw.monitors.GatewayMonitor;
import sgw.monitors.NumCounter;

public class ReceiveRequestCounter extends AbstractFilter {

    private final Logger logger = LoggerFactory.getLogger(ReceiveRequestCounter.class);

    @Override
    public String filterType(){
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter(HttpChannelContext httpCtx) {
        return true;
    }

    @Override
    public Object run(HttpChannelContext httpCtx) {
        GatewayMonitor monitor = GatewayMonitor.getInstance();
        long id = monitor.getCounter(GatewayMonitor.RCV_REQ, NumCounter.class).increase();
//        long now = System.currentTimeMillis();
//        httpCtx.put("request_start_time", now);
//        httpCtx.put("request_id", id);
//        logger.info("Receive request {} at {}", id, now);

        return null;
    }

}
