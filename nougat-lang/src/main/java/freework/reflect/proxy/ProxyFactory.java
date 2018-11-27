/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect.proxy;

import java.lang.reflect.InvocationHandler;

/**
 * Proxy factory.
 *
 * @author vacoor
 * @since 1.0
 */
public interface ProxyFactory {

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader      the class loader to define the proxy class
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param handler     the invocation handler to dispatch method invocations to
     * @param override    true if delegate all methods, false if should delegate abstract method
     * @param <T>         the proxy type
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override);

}
