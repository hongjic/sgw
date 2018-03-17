package sgw.core.load_balancer;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.annotation.ThreadSafe;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An unmodifiable implementation of round-robin style {@link LoadBalancer}
 * @param <T>
 */
@ThreadSafe
public class RoundRobinLoadBalancer<T> implements LoadBalancer<T> {

    /**
     * `items` is unmodifiable
     */
    private final T[] items;
    private int size;
    private final AtomicInteger count = new AtomicInteger(0);

    public RoundRobinLoadBalancer(Collection<T> items) {
        this.size = items.size();
        Object[] t = new Object[size];
        items.toArray(t);
        this.items = (T[]) t;
    }

    @Override
    public T next() {
        int index;
        try {
            index = Math.abs(count.getAndIncrement() % size);
        } catch (ArithmeticException e) {
            return null;
        }
        return items[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(items).iterator();
    }

    public static void main(String[] args) {
        LoadBalancer rrlb = new RoundRobinLoadBalancer(Arrays.asList("aa", "bb", "cc", "dd"));
        System.out.println(rrlb.next());
    }

}
