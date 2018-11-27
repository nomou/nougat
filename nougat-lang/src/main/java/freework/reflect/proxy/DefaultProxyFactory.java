package freework.reflect.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;

/**
 * Defaults proxy factory.
 * <p>
 * {@link JdkDynamicProxyFactory}
 * {@link JavassistProxyFactory}
 * {@link CglibProxyFactory}
 * {@link java.lang.reflect.Proxy}
 * {@link InvocationHandler}
 *
 * @author vacoor
 * @since 1.0
 */
public class DefaultProxyFactory implements ProxyFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProxyFactory.class);
    private static final boolean CGLIB_PRESENT = isPresent(CglibProxyFactory.ENHANCER_NAME, DefaultProxyFactory.class.getClassLoader());
    private static final boolean JAVASSIST_PRESENT = isPresent(JavassistProxyFactory.ENHANCER_NAME, DefaultProxyFactory.class.getClassLoader());

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        if (targetClass.isInterface()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("create jdk dynamic proxy, type: {}", targetClass);
            }
            return (T) JdkDynamicProxyFactory.newProxyInstance(loader, targetClass, handler);
        }
        if (JAVASSIST_PRESENT) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("create javassist proxy: {}", targetClass);
            }
            return (T) JavassistProxyFactory.createProxy(targetClass, handler, override);
        }
        if (CGLIB_PRESENT) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("create cglib proxy,type: {}", targetClass);
            }
            return (T) CglibProxyFactory.createProxy(loader, targetClass, handler, override);
        }
        throw new IllegalStateException("create proxy failed, CGLIB/Javassist is not available. Add CGLIB/Javassist to your classpath.");
    }

    private static boolean isPresent(final String className, final ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (final Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }
}
