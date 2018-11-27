package freework.function;

/**
 * Lazy calculation value.
 *
 * @author vacoor
 * @since 1.0
 */
public interface LazyValue<V> {

    /**
     * Gets the lazy value.
     *
     * @return the lazy value
     */
    V get();

}
