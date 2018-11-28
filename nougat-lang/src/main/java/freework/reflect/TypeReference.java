/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <pre>
 * TypeReference&lt;List&lt;String&gt;&gt; list = new TypeReference&lt;List&lt;String&gt;&gt;() {};
 * </pre>
 * This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class TypeReference<T> {
    protected final Type type;

    /**
     * Constructs a new type literal. Derives represented class from type parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     */
    protected TypeReference() {
        final Type superClass = getClass().getGenericSuperclass();

        if (superClass instanceof Class<?>) {
            // missing type parameter, should never happen
            throw new IllegalStateException("Internal error: TypeReference constructed without actual type information");
        }

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

        if (type instanceof TypeVariable<?>) {
            throw new IllegalStateException("Internal error: TypeReference constructed without actual type information");
        }
    }

    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return type;
    }
}
