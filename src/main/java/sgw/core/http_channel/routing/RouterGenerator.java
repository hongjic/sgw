package sgw.core.http_channel.routing;

public interface RouterGenerator {

    /**
     *
     * @return a generated Router.
     */
    Router generate() throws Exception;
}
