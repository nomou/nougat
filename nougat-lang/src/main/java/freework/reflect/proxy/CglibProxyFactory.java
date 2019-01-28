/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect.proxy;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * CGLIB-based proxy factory.
 *
 * @author vacoor
 * @since 1.0
 */
public class CglibProxyFactory implements ProxyFactory {
    /**
     * CGLIB enhancer class name.
     */
    public static final String ENHANCER_NAME = "net.sf.cglib.proxy.Enhancer";

    /**
     * Naming policy.
     */
    private static final NamingPolicy NAMING_POLICY = new DefaultNamingPolicy() {
        /**
         * {@inheritDoc}
         */
        @Override
        protected String getTag() {
            return super.getTag();
        }
    };

    static {
        try {
            Class.forName(ENHANCER_NAME);
        } catch (final Throwable e) {
            throw new IllegalStateException("CGLIB is not available. Add CGLIB to your classpath.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        return createProxy(loader, targetClass, handler, override);
    }


    /* *************************************************
     *
     * *************************************************/

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader      the class loader to define the proxy class
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param handler     the invocation handler to dispatch method invocations to
     * @param override    true if delegate all methods, false if should delegate abstract method
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, boolean override) {
        return (T) createProxy(loader, targetClass, handler, override, Collections.<Class<?>>emptyList(), Collections.emptyList());
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader              the class loader to define the proxy class
     * @param superclass          the superclass/interface for the proxy class to extends/implement
     * @param handler             the invocation handler to dispatch method invocations to
     * @param override            true if delegate all methods, false if should delegate abstract method
     * @param constructorArgTypes the constructor arguments types using for instantiate
     * @param constructorArgs     the constructor arguments using for instantiate
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    public static Object createProxy(final ClassLoader loader, final Class<?> superclass,
                                     final InvocationHandler handler, final boolean override,
                                     final List<Class<?>> constructorArgTypes, final List<Object> constructorArgs) {
        return createProxy(loader, superclass, createCallback(handler, override), constructorArgTypes, constructorArgs);
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader              the class loader to define the proxy class
     * @param superclass          the superclass/interface for the proxy class to extends/implement
     * @param callback            the callback to dispatch method invocations to
     * @param constructorArgTypes the constructor arguments types using for instantiate
     * @param constructorArgs     the constructor arguments using for instantiate
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    public static Object createProxy(final ClassLoader loader, final Class<?> superclass, final Callback callback,
                                     final List<Class<?>> constructorArgTypes, final List<Object> constructorArgs) {
        try {
            final Enhancer enhancer = createEnhancer();
            enhancer.setClassLoader(loader);
            enhancer.setCallback(callback);

            if (superclass.isInterface()) {
                enhancer.setInterfaces(new Class<?>[]{superclass});
            } else {
                enhancer.setSuperclass(superclass);
            }

            Object enhanced;
            if (constructorArgTypes.isEmpty()) {
                enhanced = enhancer.create();
            } else {
                final Class<?>[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
                final Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
                enhanced = enhancer.create(typesArray, valuesArray);
            }
            return enhanced;
        } catch (final Exception ex) {
            throw new IllegalStateException("create cglib proxy failed: ", ex);
        }
    }

    private static Enhancer createEnhancer() {
        final Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(NAMING_POLICY);
        return enhancer;
    }

    /**
     * Creates an invocation handler adapter for cglib.
     *
     * @param handler JDK {@link InvocationHandler}
     * @return JDK {@link InvocationHandler} 的 {@link MethodInterceptor} adapter
     */
    private static MethodInterceptor createCallback(final InvocationHandler handler, final boolean override) {
        return new InvocationHandlerCallback(handler, override);
    }

    /**
     * JDK {@link InvocationHandler} 的 {@link MethodInterceptor} adapter.
     */
    private static class InvocationHandlerCallback implements MethodInterceptor {
        private final InvocationHandler delegate;
        private final boolean override;

        private InvocationHandlerCallback(final InvocationHandler delegate, final boolean override) {
            this.delegate = delegate;
            this.override = override;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object intercept(final Object proxy, final Method method,
                                final Object[] args, final MethodProxy methodProxy) throws Throwable {
            if (!Modifier.isAbstract(method.getModifiers()) && !override) {
                return methodProxy.invokeSuper(proxy, args);
            }
            return delegate.invoke(proxy, method, args);
        }
    }
}
