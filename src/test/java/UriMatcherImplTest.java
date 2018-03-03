
import org.junit.Test;
import sgw.core.http_channel.routing.UriMatcher;
import sgw.core.http_channel.routing.UriMatcherImpl;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UriMatcherImplTest {

    UriMatcher<String> uriMatcher;
    Map<String, String> routing;
    Map<String, String> addRouting;

    public UriMatcherImplTest() {
        routing = new HashMap<>();
        routing.put("/aa/*", "router0");
        routing.put("/aa/{id:[0-9]+}", "router1");
        routing.put("/aa/?b", "router2");
        routing.put("/aa/bb", "router3");
        routing.put("/a/b/c", "router4");
        routing.put("/a/c/b", "router5");
        routing.put("/aa/{aa}/{bb}/{cc}", "router6");
        routing.put("/aa/bb/*/*", "router7");
        routing.put("/aa/bb/c{id}/dd", "router8");
        routing.put("/aa/bb/cc/dd", "router9");

        addRouting = new HashMap<>();
        addRouting.put("/a/b/c", "router10");
        addRouting.put("/", "router11");
        addRouting.put("/aa/{st:[a-z]+}", "router12");
    }

    void initialize() {
        uriMatcher = new UriMatcherImpl<>();
        for (Map.Entry<String, String> entry: routing.entrySet()) {
            assertNull(uriMatcher.register(entry.getKey(), entry.getValue()));
        }
    }

    @Test
    public void testLookup() {
        initialize();
        UriMatcher.UriMatchResult r;

        r = uriMatcher.lookup("/aa");
        assertNull(r);

        r = uriMatcher.lookup("/aa/ccc");
        assertEquals("router0", r.getObject());

        r = uriMatcher.lookup("/aa/30");
        assertEquals("router1", r.getObject());
        assertEquals("30", r.getParams().get("id"));

        r = uriMatcher.lookup("/aa/ab");
        assertEquals("router2", r.getObject());

        r = uriMatcher.lookup("/aa/bb");
        assertEquals("router3", r.getObject());

        r = uriMatcher.lookup("/a/b/c");
        assertEquals("router4", r.getObject());

        r = uriMatcher.lookup("/a/c/b");
        assertEquals("router5", r.getObject());

        r = uriMatcher.lookup("/aa/cc/dd/ee");
        assertEquals("router6", r.getObject());
        assertEquals("cc", r.getParams().get("aa"));
        assertEquals("dd", r.getParams().get("bb"));
        assertEquals("ee", r.getParams().get("cc"));

        r = uriMatcher.lookup("/aa/bb/cc/123");
        assertEquals("router7", r.getObject());

        r = uriMatcher.lookup("/aa/bb/c30/dd");
        assertEquals("router8", r.getObject());
        assertEquals("30", r.getParams().get("id"));

        r = uriMatcher.lookup("/aa/bb/cc/dd");
        assertEquals("router9", r.getObject());
    }

    @Test
    public void testRegister() {
        initialize();
        // override
        assertEquals("router4", uriMatcher.register("/a/b/c", "router10"));
        assertNull(uriMatcher.register("/", "router11"));
        assertNull(uriMatcher.register("/aa/{st:[a-z]+}", "router12"));

        // test
        UriMatcher.UriMatchResult r;
        r = uriMatcher.lookup("/a/b/c");
        assertEquals("router10", r.getObject());

        r = uriMatcher.lookup("/");
        assertEquals("router11", r.getObject());

        r = uriMatcher.lookup("/aa/ccc");
        assertEquals("router12", r.getObject());
        assertEquals("ccc", r.getParams().get("st"));
    }

    @Test
    public void testInitialize() {
        long start;
        start = System.currentTimeMillis();
        initialize();
        System.out.println("Initialize " + routing.size()
                + " mapping by 'register()' one by one, finished in: " +
                (System.currentTimeMillis() - start) + " ms.");

        start = System.currentTimeMillis();
        uriMatcher.initialize(routing);
        System.out.println("Initialize " + routing.size()
                + " mapping by 'initialize()' in batch, finished in: " +
                (System.currentTimeMillis() - start) + " ms.");
    }

    @Test
    public void testRegisterAll() {
        initialize();
        // override
        uriMatcher.registerAll(addRouting);

        // test
        UriMatcher.UriMatchResult r;
        r = uriMatcher.lookup("/a/b/c");
        assertEquals("router10", r.getObject());

        r = uriMatcher.lookup("/");
        assertEquals("router11", r.getObject());

        r = uriMatcher.lookup("/aa/ccc");
        assertEquals("router12", r.getObject());
        assertEquals("ccc", r.getParams().get("st"));
    }

    @Test
    public void testUnregister() {
        initialize();

        // remove mappings that dont exist
        assertNull(uriMatcher.unregister("/"));
        assertNull(uriMatcher.unregister("/aaaa"));
        // remove two exist mappings
        assertEquals("router1", uriMatcher.unregister("/aa/{id:[0-9]+}"));
        assertEquals("router7", uriMatcher.unregister("/aa/bb/*/*"));

        UriMatcher.UriMatchResult<String> r;
        r = uriMatcher.lookup("/aa/30");
        assertEquals("router0", r.getObject());
        assertEquals(0, r.getParams().size());

        r = uriMatcher.lookup("/aa/bb/cc/123");
        assertEquals("router6", r.getObject());
        assertEquals("bb", r.getParams().get("aa"));
        assertEquals("cc", r.getParams().get("bb"));
        assertEquals("123", r.getParams().get("cc"));

    }

    @Test
    public void testUnregisterAll() {
        initialize();

        Collection<String> removeRouting = Arrays.asList("/", "/aaaa", "/aa/{id:[0-9]+}", "/aa/bb/*/*");
        uriMatcher.unregisterAll(removeRouting);

        UriMatcher.UriMatchResult<String> r;
        r = uriMatcher.lookup("/aa/30");
        assertEquals("router0", r.getObject());
        assertEquals(0, r.getParams().size());

        r = uriMatcher.lookup("/aa/bb/cc/123");
        assertEquals("router6", r.getObject());
        assertEquals("bb", r.getParams().get("aa"));
        assertEquals("cc", r.getParams().get("bb"));
        assertEquals("123", r.getParams().get("cc"));
    }
}
