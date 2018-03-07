package sgw.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.filters.AbstractFilter.FilterException;
import sgw.core.util.FastMessage;

import java.util.List;

/**
 * singleton and stateless  --> thread safe
 */
public enum FilterProcessor {

    Instance;

    private final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);

    public void preRouting(HttpChannelContext httpCtx) throws FilterException {
        runFilters("pre", httpCtx);
    }

    public void postRouting(HttpChannelContext httpCtx) throws FilterException {
        runFilters("post", httpCtx);
    }

    public void runFilters(String filterType, HttpChannelContext httpCtx) throws FilterException {
        List<AbstractFilter> list = FilterMngr.Instance.getFiltersByType(filterType);
        if (list != null) {
            for (AbstractFilter filter: list) {
                FilterResult result = filter.runFilter(httpCtx);

                logger.debug("Filter {}: {}", filter.getClass().getName(), result.toString());
                if (result.getStatus() == FilterExecutionStatus.FAILED) {
                    Exception e = result.getException();
                    if (!(e instanceof FilterException))
                        e = new FilterException(e);
                    httpCtx.setSendFastMessage(true);
                    httpCtx.setFastMessage(new FastMessage(e));

                    throw (FilterException) e;
                }
            }
        }
    }

}
