package sgw.core.load_balancer;

import java.util.Collection;

public interface LoadBalancer<T> extends Iterable<T> {

    /**
     * @return use the underlying load balancing strategy to get an object, null if size = 0
     */
    T next();

    /**
     * @return number of objects inside
     */
    int size();

}
