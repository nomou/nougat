package freework.util;

/**
 * Array factory.
 *
 * @author vacoor
 * @since 1.0
 */
public interface ArrayFactory<E> {

    /**
     * Creates an array of the given length.
     *
     * @param len the length
     * @return the new array
     */
    E[] create(final int len);

}
