package freework.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * JDK-based proxy factory.
 *
 * @author vacoor
 * @since 1.0
 */
public class JdkDynamicProxyFactory implements ProxyFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        return (T) newProxyInstance(loader, targetClass, handler);
    }

    /**
     * Creates an instance of a proxy class for the specified interfaces that dispatches method invocations
     * to the specified invocation handler.
     *
     * @param loader      the class loader to define the proxy class
     * @param targetClass the interface for the proxy class to implement
     * @param handler     the invocation handler to dispatch method invocations to
     * @return a proxy instance with the specified invocation handler of a proxy class that
     * is defined by the specified class loader and that implements the specified interface
     */
    public static Object newProxyInstance(final ClassLoader loader, final Class<?> targetClass, final InvocationHandler handler) {
        return Proxy.newProxyInstance(loader, new Class<?>[]{targetClass}, handler);
    }
}
