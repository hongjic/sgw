import org.junit.Test;
import sgw.core.service_discovery.LoadBalancer;
import sgw.core.service_discovery.RoundRobinLoadBalancer;

import static org.junit.Assert.*;

public class RoundRobinLoadBalancerTest {

    @Test
    public void testNormal() {
        LoadBalancer<String> lb = new RoundRobinLoadBalancer<>();
        assertEquals(lb.add("aaa"), 1);
        assertEquals(lb.next(), "aaa");
        assertEquals(lb.next(), "aaa");

        assertEquals(lb.add("bbb"), 2);
        assertEquals(lb.next(), "aaa");
        assertEquals(lb.next(), "bbb");

        assertEquals(lb.add("ccc"), 3);
        assertEquals(lb.next(), "bbb");
        assertEquals(lb.next(), "ccc");

        assertEquals(lb.remove("bbb"), 2);
        assertEquals(lb.next(), "aaa");
        assertEquals(lb.next(), "ccc");
    }

    // test add() and next()
    @Test
    public void testConcurrent1() {
        final LoadBalancer<String> lb = new RoundRobinLoadBalancer<>();
        final int[] stat = new int[2000];
        Thread nextThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String item = lb.next();
                if (item != null)
                    stat[Integer.valueOf(item)] ++;
            }
        });
        Thread addThread = new Thread(() -> {
            for (int i = 0; i < 2000; i ++) {
                lb.add(String.valueOf(i));
                try {
                    Thread.sleep(5);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            nextThread.interrupt();
        });

        try {
            nextThread.start();
            addThread.start();
            nextThread.join();
            addThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 2000; i ++)
            System.out.println(i + ": " + stat[i]);

    }

    // test remove() and next()
    @Test
    public void testConcurrent2() {
        final LoadBalancer<String> lb = new RoundRobinLoadBalancer<>();
        for (int i = 0; i < 10000; i ++)
            lb.add(String.valueOf(i));
        final int[] stat = new int[10000];

        Thread nextThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                String item = lb.next();
                if (item != null)
                    stat[Integer.valueOf(item)] ++;
            }
        });

        Thread removeThread = new Thread(() -> {
            for (int i = 0; i < 10000; i ++) {
                lb.remove(String.valueOf(i));
            }
            nextThread.interrupt();
        });

        try {
            nextThread.start();
            removeThread.start();
            nextThread.join();
            removeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        for (int i = 0; i < 100000; i ++)
//            System.out.println(i + ": " + stat[i]);

    }

}
