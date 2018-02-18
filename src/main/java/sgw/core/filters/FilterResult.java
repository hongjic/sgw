package sgw.core.filters;

public final class FilterResult {

    private Object result;
    private Exception exception;
    private FilterExecutionStatus status;

    public FilterResult(Object result, FilterExecutionStatus status) {
        this.result = result;
        this.status = status;
    }

    public FilterResult(FilterExecutionStatus status) {
        this.status = status;
    }

    /**
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return the status
     */
    public FilterExecutionStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(FilterExecutionStatus status) {
        this.status = status;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return status.name();
    }
}
