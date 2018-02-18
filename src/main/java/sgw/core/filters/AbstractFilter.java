package sgw.core.filters;

import sgw.core.http_channel.HttpChannelContext;

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
    abstract public boolean shouldFilter(HttpChannelContext httpCtx);

    /**
     * use {@link HttpChannelContext#setContinueProcessing(boolean)} to stop moving forward.
     * @return maybe useful in future
     */
    abstract public Object run(HttpChannelContext httpCtx);

    public FilterResult runFilter(HttpChannelContext httpCtx) {
        FilterResult fr;
        if (shouldFilter(httpCtx)) {
            try {
                Object result = run(httpCtx);
                fr = new FilterResult(result, FilterExecutionStatus.SUCCESS);
            } catch (Exception e) {
                FilterException fe = new FilterException(e);
                fr = new FilterResult(FilterExecutionStatus.FAILED);
                fr.setException(fe);
            }
        }
        else {
            fr = new FilterResult(FilterExecutionStatus.SKIPEED);
        }
        return fr;
    }


    public static class FilterException extends Exception {

        public FilterException(Exception e) {
            super(e);
        }
    }
}
