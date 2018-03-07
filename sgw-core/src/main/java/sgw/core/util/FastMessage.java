package sgw.core.util;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * used for filter customized response and operational response.
 */
public final class FastMessage {

    private Exception exception;
    private HttpResponseStatus status;

    private String message;

    static FastMessage emptyMessage() {
        FastMessage message = new FastMessage("Request filtered, but no response message specified.");
        message.setHttpResponseStatus(HttpResponseStatus.OK);
        return message;
    }

    public static final FastMessage EMPTY = emptyMessage();

    public FastMessage(Exception e) {
        this.exception = e;
        status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public FastMessage(String message) {
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
            return message;
    }
}
