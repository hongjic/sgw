package sgw.core.filters;

import sgw.core.http_channel.HttpRequestContext;
import sgw.core.util.FastMessage;

public abstract class AbstractFilter {

    /**
     * @return A string representing filter type: {"pre", "post"}
     */
    abstract public String filterType();

    /**
     * @return the int order of the filter in all filters with the same type
     */
    abstract public int filterOrder();

    /**
     * @return true if this filter should run
     */
    abstract public boolean shouldFilter(HttpRequestContext httpCtx);

    /**
     *
     * @return A {@link FastMessage} instance to send back, null if nothing happen.
     */
    abstract public FastMessage run(HttpRequestContext reqCtx);

}
