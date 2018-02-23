package sgw.core.http_channel.routing;

import sgw.core.http_channel.HttpRequestDef;

public class UndefinedHttpRequestException extends Exception {

    public UndefinedHttpRequestException(HttpRequestDef httpDef) {
        super("http request: " + httpDef.toString() + " not defined.");
    }
}
