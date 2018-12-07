/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect.proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * Javassist-based proxy factory.
 *
 * @author vacoor
 * @since 1.0
 */
public final class JavassistProxyFactory implements ProxyFactory {
    public static final String ENHANCER_NAME = "javassist.util.proxy.ProxyFactory";

    static {
        try {
            Class.forName(ENHANCER_NAME);
        } catch (Throwable e) {
            throw new IllegalStateException("Javassist is not available. Add Javassist to your classpath.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        return createProxy(targetClass, handler, override);
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param h           the invocation handler to dispatch method invocations to
     * @param override    true if delegate all methods, false if should delegate abstract method
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<T> targetClass, final InvocationHandler h, final boolean override) {
        return (T) createProxy(targetClass, h, override, Collections.<Class<?>>emptyList(), Collections.emptyList());
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param targetClass         the superclass/interface for the proxy class to extends/implement
     * @param h                   the invocation handler to dispatch method invocations to
     * @param override            true if delegate all methods, false if should delegate abstract method
     * @param constructorArgTypes the constructor arguments types using for instantiate
     * @param constructorArgs     the constructor arguments using for instantiate
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    public static Object createProxy(final Class<?> targetClass, final InvocationHandler h, final boolean override,
                                     final List<Class<?>> constructorArgTypes, final List<Object> constructorArgs) {
        return createProxy(targetClass, createHandler(h, override), constructorArgTypes, constructorArgs);
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param superclass          the superclass/interface for the proxy class to extends/implement
     * @param callback            the callback to dispatch method invocations to
     * @param constructorArgTypes the constructor arguments types using for instantiate
     * @param constructorArgs     the constructor arguments using for instantiate
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    public static Object createProxy(final Class<?> superclass, final MethodHandler callback,
                                     final List<Class<?>> constructorArgTypes, final List<Object> constructorArgs) {
        final javassist.util.proxy.ProxyFactory enhancer = createEnhancer();
        if (superclass.isInterface()) {
            enhancer.setInterfaces(new Class<?>[]{superclass});
        } else {
            enhancer.setSuperclass(superclass);
        }

        Object enhanced;
        final Class<?>[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
        final Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
        try {
            enhanced = enhancer.create(typesArray, valuesArray);
        } catch (Exception e) {
            throw new RuntimeException("Error creating lazy proxy.  Cause: " + e, e);
        }
        ((ProxyObject) enhanced).setHandler(callback);
        return enhanced;
    }

    private static javassist.util.proxy.ProxyFactory createEnhancer() {
        return new javassist.util.proxy.ProxyFactory();
    }

    /**
     * Creates an invocation handler adapter for javassist.
     *
     * @param h JDK {@link InvocationHandler}
     * @return JDK {@link InvocationHandler} 的 {@link MethodHandler} adapter
     */
    private static MethodHandler createHandler(final InvocationHandler h, final boolean override) {
        return new InvocationHandlerCallback(h, override);
    }

    /**
     * JDK {@link InvocationHandler} 的 {@link MethodHandler} adapter.
     */
    private static class InvocationHandlerCallback implements MethodHandler {
        private final InvocationHandler delegate;
        private final boolean override;

        private InvocationHandlerCallback(final InvocationHandler delegate, final boolean override) {
            this.delegate = delegate;
            this.override = override;
        }

        @Override
        @SuppressWarnings("PMD.RemoveCommentedCodeRule")
        public Object invoke(final Object proxy, final Method method, final Method methodProxy, final Object[] args) throws Throwable {
            if (!Modifier.isAbstract(method.getModifiers()) && !override) {
                /*-
                 * not override method implementations, invoke super.
                 * return delegate.invoke(proxy, methodProxy, args);
                 */
                return methodProxy.invoke(proxy, args);
            }
            return delegate.invoke(proxy, method, args);
        }
    }
}
