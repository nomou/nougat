/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * A ThreadContext provides a means of binding and unbinding objects to the
 * current thread based on key/value pairs.
 * <p>
 * <p>An internal {@link java.util.HashMap} is used to maintain the key/value pairs
 * for each thread.</p>
 * <p>
 * <p>If the desired behavior is to ensure that bound data is not shared across
 * threads in a pooled or reusable threaded environment, the application (or more likely a framework) must
 * bind and remove any necessary values at the beginning and end of stack
 * execution, respectively (i.e. individually explicitly or all via the <tt>clear</tt> method).</p>
 *
 * @see #remove()
 * @since 0.1
 */
public abstract class ThreadContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadContext.class);

    private static final ThreadLocal<Map<Object, Object>> resources = new InheritableThreadLocalMap<Map<Object, Object>>();

    /**
     * Returns the ThreadLocal Map. This Map is used internally to bind objects
     * to the current thread by storing each object under a unique key.
     *
     * @return the map of bound resources
     */
    public static Map<Object, Object> getResources() {
        return resources != null ? new HashMap<Object, Object>(resources.get()) : null;
    }

    /**
     * Allows a caller to explicitly set the entire resource map.  This operation overwrites everything that existed
     * previously in the ThreadContext - if you need to retain what was on the thread prior to calling this method,
     * call the {@link #getResources()} method, which will give you the existing state.
     *
     * @param newResources the resources to replace the existing {@link #getResources() resources}.
     * @since 1.0
     */
    public static void setResources(Map<Object, Object> newResources) {
        if (null == newResources) {
            return;
        }

        final Map<Object, Object> res = resources.get();
        res.clear();
        res.putAll(newResources);
    }

    /**
     * Returns the value bound in the {@code ThreadContext} under the specified {@code key}, or {@code null} if there
     * is no value for that {@code key}.
     *
     * @param key the map key to use to lookup the value
     * @return the value bound in the {@code ThreadContext} under the specified {@code key}, or {@code null} if there
     * is no value for that {@code key}.
     * @since 1.0
     */
    private static Object getValue(Object key) {
        return resources.get().get(key);
    }

    /**
     * Returns the object for the specified <code>key</code> that is bound to
     * the current thread.
     *
     * @param key the key that identifies the value to return
     * @return the object keyed by <code>key</code> or <code>null</code> if
     * no value exists for the specified <code>key</code>
     */
    public static Object get(Object key) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("get() - in thread [{}]", Thread.currentThread().getName());
        }

        Object value = getValue(key);
        if ((value != null) && LOGGER.isTraceEnabled()) {
            LOGGER.trace("Retrieved value of type [{}] for key [{}] " + "bound to thread [{}]", value.getClass().getName(), key, Thread.currentThread().getName());
        }
        return value;
    }

    /**
     * Binds <tt>value</tt> for the given <code>key</code> to the current thread.
     * <p>
     * <p>A <tt>null</tt> <tt>value</tt> has the same effect as if <tt>remove</tt> was called for the given
     * <tt>key</tt>, i.e.:
     * <p>
     * <pre>
     * if ( value == null ) {
     *     remove( key );
     * }</pre>
     *
     * @param key   The key with which to identify the <code>value</code>.
     * @param value The value to bind to the thread.
     * @throws IllegalArgumentException if the <code>key</code> argument is <tt>null</tt>.
     */
    public static void put(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }

        if (value == null) {
            remove(key);
            return;
        }

        resources.get().put(key, value);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Bound value of type [{}] for key [{}] to thread " + "[{}]", value.getClass().getName(), key, Thread.currentThread().getName());
        }
    }

    /**
     * Unbinds the value for the given <code>key</code> from the current
     * thread.
     *
     * @param key The key identifying the value bound to the current thread.
     * @return the object unbound or <tt>null</tt> if there was mux bound
     * under the specified <tt>key</tt> name.
     */
    public static Object remove(Object key) {
        Object value = resources.get().remove(key);

        if ((value != null) && LOGGER.isTraceEnabled()) {
            LOGGER.trace("Removed value of type [{}] for key [{}]" + "from thread [{}]", value.getClass().getName(), key, Thread.currentThread().getName());
        }

        return value;
    }

    /**
     * {@link ThreadLocal#remove Remove}s the underlying {@link ThreadLocal ThreadLocal} from the thread.
     * <p>
     * This method is meant to be the final 'clean up' operation that is called at the end of thread execution to
     * prevent thread corruption in pooled thread environments.
     *
     * @since 1.0
     */
    public static void remove() {
        resources.remove();
    }

    /**
     * Default no-argument constructor.
     */
    protected ThreadContext() {
    }

    /* ******************************
     *
     * ******************************/

    private static final class InheritableThreadLocalMap<T extends Map<Object, Object>> extends InheritableThreadLocal<Map<Object, Object>> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<Object, Object>();
        }

        /**
         * This implementation was added to address a
         * <a href="http://jsecurity.markmail.org/search/?q=#query:+page:1+mid:xqi2yxurwmrpqrvj+state:results">
         * user-reported issue</a>.
         *
         * @param parentValue the parent value, a HashMap as defined in the {@link #initialValue()} method.
         * @return the HashMap to be used by any parent-spawned child threads (a clone of the parent HashMap).
         */
        @Override
        @SuppressWarnings({"unchecked"})
        protected Map<Object, Object> childValue(Map<Object, Object> parentValue) {
            if (parentValue != null) {
                return (Map<Object, Object>) ((HashMap<Object, Object>) parentValue).clone();
            } else {
                return null;
            }
        }
    }
}

