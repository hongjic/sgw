package sgw.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpChannelContext;
import sgw.core.filters.AbstractFilter.FilterException;

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
        List<AbstractFilter> list = FilterLoader.Instance.getFiltersByType(filterType);
        if (list != null) {
            for (AbstractFilter filter: list) {
                FilterResult result = filter.runFilter(httpCtx);
                logger.info("Filter {}: {}", filter.getClass().getName(), result.toString());
                if (result.getStatus() == FilterExecutionStatus.FAILED) {
                    Exception e = result.getException();
                    if (e instanceof FilterException)
                        throw (FilterException) e;
                    else
                        throw new FilterException(e);
                }
            }
        }
    }

}
