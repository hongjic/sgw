package sgw.core.filters;

import io.netty.handler.codec.http.HttpResponseStatus;
import sgw.core.filters.AbstractFilter.FilterException;

public final class FastMessage {

    private FilterException exception;
    private HttpResponseStatus status;

    // override `toString()` to customize response body.
    private Object message;

    static FastMessage emptyMessage() {
        FastMessage message = new FastMessage("Request filtered, but no response message specified.");
        message.setHttpResponseStatus(HttpResponseStatus.OK);
        return message;
    }

    public static final FastMessage EMPTY = emptyMessage();

    public FastMessage(FilterException e) {
        this.exception = e;
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public FastMessage(Object message) {
        this.message = message;
        status = HttpResponseStatus.OK;
    }

    public void setHttpResponseStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public String getResponseBody() {
        if (exception != null)
            return exception.getMessage();
        else
            return message.toString();
    }
}
