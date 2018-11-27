package freework.function;

/**
 * Determines an output value based on an input value.
 *
 * @param <F> input type
 * @param <T> output type
 * @author vacoor
 * @since 1.0
 */
public interface Function<F, T> {

    /**
     * Returns the result of applying this function to {@code input}.
     *
     * @param input the input
     * @return the output
     */
    T apply(final F input);

}
