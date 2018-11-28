/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils for primitive types and their corresponding wrapper types.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Primitives {
    private static final Map<String, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE = new HashMap<String, Class<?>>(9);
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE = new HashMap<Class<?>, Class<?>>(9);

    static {
        PRIMITIVE_TO_WRAPPER_TYPE.put(Boolean.TYPE.getName(), Boolean.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Character.TYPE.getName(), Character.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Byte.TYPE.getName(), Byte.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Short.TYPE.getName(), Short.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Integer.TYPE.getName(), Integer.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Long.TYPE.getName(), Long.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Float.TYPE.getName(), Float.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Double.TYPE.getName(), Double.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Void.TYPE.getName(), Void.class);

        WRAPPER_TO_PRIMITIVE_TYPE.put(Boolean.class, Boolean.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Character.class, Character.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Byte.class, Byte.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Short.class, Short.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Integer.class, Integer.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Long.class, Long.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Float.class, Float.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Double.class, Double.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Void.class, Void.TYPE);
    }

    /**
     * Non-instantiate.
     */
    private Primitives() {
    }

    /**
     * Returns {@code true} if {@code type} is one of the nine primitive-wrapper types.
     *
     * @param type the type
     * @return true if one of the nine primitive-wrapper types
     */
    public static boolean isWrapperType(final Class<?> type) {
        return WRAPPER_TO_PRIMITIVE_TYPE.containsKey(type);
    }

    /**
     * Returns the corresponding wrapper type of 'type', if it is a primitive type name; otherwise returns null.
     * <p>Idempotent.
     * <pre>
     *     wrap("int") == Integer.class
     *     wrap("String") == null
     * </pre>
     *
     * @param type the name of primitive type
     * @return the corresponding wrapper type if 'type' is a primitive type name, otherwise null
     */
    public static Class<?> wrap(final String type) {
        return PRIMITIVE_TO_WRAPPER_TYPE.get(type);
    }

    /**
     * Returns the corresponding wrapper type of 'type', if it is a primitive type; otherwise returns 'type' itself.
     * <p>Idempotent.
     * <pre>
     *     wrap(int.class) == Integer.class
     *     wrap(Integer.class) == Integer.class
     *     wrap(String.class) == String.class
     * </pre>
     *
     * @param type the primitive type
     * @return the corresponding wrapper type if 'type' is a primitive type, otherwise 'type'
     */
    public static Class<?> wrap(final Class<?> type) {
        final Class<?> wrapped = wrap(type.getName());
        return wrapped != null ? wrapped : type;
    }

    /**
     * Returns the corresponding wrapper types of {@code types}.
     *
     * @param types the types
     * @return the wrapper types
     * @see #wrap(Class)
     */
    public static Class<?>[] wrap(final Class<?>[] types) {
        final Class<?>[] wrapped = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            wrapped[i] = wrap(types[i]);
        }
        return wrapped;
    }

    /**
     * Returns the corresponding primitive type of {@code type} if it is a wrapper type; otherwise returns {@code type} itself.
     *
     * <p>Idempotent.
     * <pre>
     *     unwrap(Integer.class) == int.class
     *     unwrap(int.class) == int.class
     *     unwrap(String.class) == String.class
     * </pre>
     *
     * @param type the type
     * @return primitive type if it is a wrapper type, otherwise itself
     */
    public static Class<?> unwrap(final Class<?> type) {
        final Class<?> unwrapped = WRAPPER_TO_PRIMITIVE_TYPE.get(type);
        return unwrapped != null ? unwrapped : type;
    }

    /**
     * Returns the corresponding primitive types of {@code types}.
     *
     * @param types the types
     * @return the primitive types
     * @see #unwrap(Class)
     */
    public static Class<?>[] unwrap(final Class<?>[] types) {
        final Class<?>[] unwrapped = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            unwrapped[i] = unwrap(types[i]);
        }
        return unwrapped;
    }
}
