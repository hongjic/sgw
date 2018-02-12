package sgw.core.service_discovery;

/**
 * A collection of existing objects
 * All methods should be thread-safe
 * @param <T> object type
 */
public interface LoadBalancer<T> {

    /**
     * @return use the underlying load balancing strategy to get an object.
     */
    T next();

    /**
     * @param item the new object to be added to the collection
     * @return size of collection after add finishes.
     */
    int add(T item);

    /**
     * @param item the object to be removed from the collection
     * @return the size of collection after remove finishes.
     */
    int remove(T item);

}
