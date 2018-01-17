package sgw.conversion;

/**
 * stateless data convertor
 * @param <X> input data format
 * @param <Y> output data format
 */
public interface DataConvertor<X, Y> {

    Y convert(X input) throws Exception;
}
