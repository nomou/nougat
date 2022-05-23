package freework.reflect;


import freework.util.LazyValue;
import freework.reflect.proxy.DefaultProxyFactory;
import freework.reflect.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy2 provides static methods for creating proxy objects on runtime,
 * and unlike JDK dynamic proxy, Proxy2 is not mandatory to create proxy instances based on interfaces.
 * <p>If the given class is an interface, the jdk dynamic proxy will be used, otherwise cglib / javassist will be tried.
 *
 * @author vacoor
 * @since v1.0
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class Proxy2 {
    private static final ProxyFactory DEFAULT = new DefaultProxyFactory();

    /**
     * Non-instantiate.
     */
    private Proxy2() {
    }

    /**
     * Creates an instance of a lazy compute proxy class for the specified superclass/interfaces that dispatches method
     * invocations to the lazy computed result.
     *
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param value       the lazy value(compute result must be not null)
     * @param <T>         the proxy type
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the default class loader and that extends/implements the specified superclass/interface
     */
    public static <T> T newLazyProxyInstance(final Class<T> targetClass, final LazyValue<T> value) {
        return newLazyProxyInstance(null, targetClass, value);
    }

    /**
     * Creates an instance of a lazy compute proxy class for the specified superclass/interfaces that dispatches method
     * invocations to the lazy computed result.
     *
     * @param loader      the class loader to define the proxy class
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param value       the lazy value(compute result must be not null)
     * @param <T>         the proxy type
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the default class loader and that extends/implements the specified superclass/interface
     */
    public static <T> T newLazyProxyInstance(final ClassLoader loader, final Class<T> targetClass, final LazyValue<T> value) {
        return newProxyInstance(loader, targetClass, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                final T computed = value.get();
                if (null == computed) {
                    throw new IllegalStateException("lazy value is null");
                }
                return method.invoke(computed, args);
            }
        }, true);
    }


    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param h           the invocation handler to dispatch method invocations to
     * @param override    true if delegate all methods, false if should delegate abstract method
     * @param <T>         the proxy type
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the default class loader and that extends/implements the specified superclass/interface
     */
    public static <T> T newProxyInstance(final Class<T> targetClass, final InvocationHandler h, final boolean override) {
        return newProxyInstance(null, targetClass, h, override);
    }

    /**
     * Creates an instance of a proxy class for the specified superclass/interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader      the class loader to define the proxy class
     * @param targetClass the superclass/interface for the proxy class to extends/implement
     * @param h           the invocation handler to dispatch method invocations to
     * @param override    true if delegate all methods, false if should delegate abstract method
     * @param <T>         the proxy type
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that extends/implements the specified superclass/interface
     */
    public static <T> T newProxyInstance(ClassLoader loader, final Class<T> targetClass, final InvocationHandler h, final boolean override) {
        if (null == loader) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        if (null == loader) {
            loader = targetClass.getClassLoader();
        }
        if (null == loader) {
            loader = Proxy2.class.getClassLoader();
        }
        return getDefaultProxyFactory().getProxy(loader, targetClass, h, override);
    }

    /**
     * Gets the default proxy factory.
     */
    private static ProxyFactory getDefaultProxyFactory() {
        return DEFAULT;
    }
}
