/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect;


import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a method parameter.
 * <p><pre>
 *     Parameter.lookup(method)[0].isCompatible(Object.class);
 * </pre>
 *
 * @author vacoor
 * @since 1.0
 */
public class Parameter implements AnnotatedElement {
    /**
     * The method declaring this parameter.
     */
    private final Method method;

    /**
     * The index of the parameter.
     */
    private final int index;

    /**
     * The declared annotations of this parameter.
     */
    private final Map<Class<? extends Annotation>, Annotation> declaredAnnotations = new HashMap<Class<? extends Annotation>, Annotation>();

    /**
     * Lookup parameters object from specified method.
     *
     * @param method the method
     * @return the parameters
     */
    public static Parameter[] lookup(final Method method) {
        if (null == method) {
            throw new IllegalArgumentException("method must not be null");
        }

        final Class<?>[] types = method.getParameterTypes();
        final Parameter[] params = new Parameter[types.length];
        for (int i = 0; i < types.length; i++) {
            params[i] = new Parameter(method, i);
        }
        return params;
    }

    /**
     * Creates a parameter instance.
     *
     * @param method the method which defines this parameter
     * @param index  the index of this parameter
     */
    private Parameter(final Method method, final int index) {
        this.method = method;
        this.index = index;

        final Annotation[] annotations = method.getParameterAnnotations()[index];
        for (final Annotation anno : annotations) {
            this.declaredAnnotations.put(anno.annotationType(), anno);
        }
    }

    /**
     * Return the method which declares this parameter.
     *
     * @return the method declaring this parameter.
     */
    public Method getDeclaringMethod() {
        return method;
    }

    /**
     * The index of the parameter.
     *
     * @return the index of the parameter
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the declared type for this parameter.
     *
     * @return the declared type for this parameter.
     */
    public Class<?> getType() {
        return method.getParameterTypes()[index];
    }

    /**
     * Returns the parameterized  type for this parameter.
     *
     * @return the parameterized  type for this parameter
     */
    public Type getParameterizedType() {
        return method.getGenericParameterTypes()[index];
    }

    /**
     * Returns true if this parameter represents a variable argument list; returns false otherwise.
     *
     * @return true if an only if this parameter represents a variable argument list.
     */
    public boolean isVarArgs() {
        return method.isVarArgs() && index == method.getParameterTypes().length - 1;
    }

    /**
     * Whether the parameter is compatible with the value of the given type.
     *
     * @param type the actual type
     * @return true if can apply
     */
    public boolean isCompatible(final Class<?> type) {
        return getType().isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return (T) declaredAnnotations.get(annotationClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Annotation[] getAnnotations() {
        return method.getParameterAnnotations()[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }
}
