package sgw.core.http_channel;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestDef {

    private final HttpMethod httpMethod;
    private final String uri;
    private final Map<String, String> params = new HashMap<>();


    public HttpRequestDef(HttpRequest request) {
        this(request.method(), request.uri());
    }

    public HttpRequestDef(HttpMethod method, String uri) {
        this.httpMethod = method;
        this.uri = uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public boolean containsParams() {
        return (params != null && params.size() == 0);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void addParsedParams(Map<String, String> map) {
        params.putAll(map);
    }

//    @Override
//    public int hashCode() {
//        return httpMethod.hashCode() * 31 + uri.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof HttpRequestDef))
//            return false;
//        HttpRequestDef other = (HttpRequestDef) o;
//        if (other.httpMethod.equals(httpMethod) && other.uri.equals(uri))
//            return true;
//        return false;
//    }

    @Override
    public String toString() {
        return String.format("[method: %s, uri: %s]", httpMethod, uri);
    }
}
