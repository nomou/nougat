package freework.util;

/**
 * Lazy compute value.
 *
 * @author vacoor
 * @since 1.1.4
 */
public abstract class LazyValue<V> {
    private volatile V value;
    private volatile boolean computed = false;

    /**
     * Gets the lazy compute value.
     *
     * @return the lazy value
     */
    public final V get() {
        if (computed) {
            return value;
        }

        synchronized (this) {
            if (!computed) {
                value = compute();
                computed = true;
            }
        }
        return value;
    }

    /**
     * Computes lazy value.
     *
     * @return the lazy value
     */
    protected abstract V compute();

    public static <V> LazyValue<V> of(final V value) {
        final LazyValue<V> lazy = new LazyValue<V>() {
            @Override
            protected V compute() {
                return value;
            }
        };
        lazy.value = value;
        lazy.computed = true;
        return lazy;
    }

    public static <V> LazyValue<V> empty() {
        return of(null);
    }
}