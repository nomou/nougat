package freework.function;

/**
 * Lazy compute value.
 *
 * @author vacoor
 * @since 1.0
 */
public interface LazyValue<V> {

    /**
     * Gets the lazy compute value.
     *
     * @return the lazy value
     */
    V get();

}
