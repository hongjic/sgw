package sgw.monitors;

public interface Counter<T> {

    /**
     * Get the current counted value.
     * @return current value
     */
    T getValue();

    /**
     *
     * @return the name of the counter.
     */
    String getName();
}
