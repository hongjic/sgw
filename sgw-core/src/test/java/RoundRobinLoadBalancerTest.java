import org.junit.Test;
import sgw.core.load_balancer.RoundRobinLoadBalancer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RoundRobinLoadBalancerTest {

    RoundRobinLoadBalancer lb = new RoundRobinLoadBalancer(Arrays.asList("aa", "bb", "cc", "dd"));

    @Test
    public void testNext() {
        assertEquals("aa", lb.next());
        assertEquals("bb", lb.next());
        assertEquals("cc", lb.next());
        assertEquals("dd", lb.next());

    }

    @Test
    public void testNextConcurrent() {
        Map<String, AtomicInteger> map = new HashMap<>();
        map.put("aa", new AtomicInteger(0));
        map.put("bb", new AtomicInteger(0));
        map.put("cc", new AtomicInteger(0));
        map.put("dd", new AtomicInteger(0));

        Runnable p = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 40000; i ++)
                    map.get(lb.next()).incrementAndGet();
            }
        };
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i ++) {
            threads[i] = new Thread(p);
        }
        for (int i = 0; i < 10; i ++) {
            threads[i].start();
        }
        try {
            for (int i = 0; i < 10; i++) {
                threads[i].join();
            }
        } catch (Exception e) {}

        assertEquals(100000, map.get("aa").get());
        assertEquals(100000, map.get("bb").get());
        assertEquals(100000, map.get("cc").get());
        assertEquals(100000, map.get("dd").get());

    }
}
