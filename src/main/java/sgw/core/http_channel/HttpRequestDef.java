package sgw.core.http_channel;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

public class HttpRequestDef {

    private final HttpMethod httpMethod;
    private final String uri;

    public HttpRequestDef(HttpRequest request) {
        this(request.method(), request.uri());
    }

    public HttpRequestDef(HttpMethod method, String uri) {
        httpMethod = method;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return httpMethod.hashCode() * 31 + uri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpRequestDef))
            return false;
        HttpRequestDef other = (HttpRequestDef) o;
        if (other.httpMethod.equals(httpMethod) && other.uri.equals(uri))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return String.format("[method: %s, uri: %s]", httpMethod, uri);
    }
}
