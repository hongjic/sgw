package sgw.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.http_channel.HttpRequestContext;
import sgw.core.util.FastMessage;

import java.util.List;

/**
 * singleton and stateless  --> thread safe
 */
public enum FilterProcessor {

    Instance;

    private final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);

    public void preRouting(HttpRequestContext reqCtx) throws FilterException {
        runFilters("pre", reqCtx);
    }

    public void postRouting(HttpRequestContext reqCtx) throws FilterException {
        runFilters("post", reqCtx);
    }

    /**
     * Throws a {@link FilterException} if filter did not pass.
     *
     * @param filterType "post", "pre", "routing"
     * @param reqCtx http request context
     * @throws FilterException if filter didn't pass
     */
    public void runFilters(String filterType, HttpRequestContext reqCtx) throws FilterException {
        List<AbstractFilter> list = FilterMngr.Instance.getFiltersByType(filterType);
        if (list != null) {
            for (AbstractFilter filter: list) {
                FastMessage result;
                try {
                    result = filter.run(reqCtx);
                } catch (Exception e) {
                    throw new FilterException(new FastMessage(reqCtx.getChannelRequestId(), e));
                }

                if (result == null)
                    logger.debug("Filter {}: passed.", filter.getClass().getName());
                else {
                    logger.debug("Filter {}: send back status {}", filter.getClass().getName(), result.getStatus().code());
                    throw new FilterException(result);
                }
            }
        }
    }

}
