package freework.function;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * A lazy value that may be updated atomically.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class AtomicLazyValue<V> implements LazyValue<V> {
    private final AtomicMarkableReference<V> ref = new AtomicMarkableReference<V>(null, false);

    /**
     * {@inheritDoc}
     */
    @Override
    public final V get() {
        if (ref.isMarked()) {
            return ref.getReference();
        }

        synchronized (ref) {
            if (!ref.isMarked()) {
                ref.compareAndSet(null, compute(), false, true);
            }
        }
        return ref.getReference();
    }

    /**
     * Computes lazy value.
     *
     * @return the lazy value
     */
    protected abstract V compute();

}
