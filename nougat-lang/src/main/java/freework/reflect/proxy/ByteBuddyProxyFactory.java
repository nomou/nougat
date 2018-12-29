package freework.reflect.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Byte-buddy-based proxy factory.
 *
 * @author vacoor
 * @since 1.1
 */
public class ByteBuddyProxyFactory implements ProxyFactory {
    public static final String ENHANCER_NAME = "net.bytebuddy.ByteBuddy";
    private static final TypeCache CACHE = new TypeCache.WithInlineExpunction(TypeCache.Sort.SOFT);

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        return createProxy(loader, targetClass, handler, override);
    }

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
    public static <T> T createProxy(final ClassLoader loader, final Class<T> targetClass, final InvocationHandler handler, final boolean override) {
        final TypeCache.SimpleKey key = new TypeCache.SimpleKey(Collections.singletonList(targetClass));
        final Class<?> clazz = CACHE.findOrInsert(loader, key, new Callable<Class<? extends T>>() {
            @Override
            public Class<? extends T> call() throws Exception {
                final DynamicType.Builder<T> builder = new ByteBuddy()
                        .ignore(isSynthetic())
                        .with(TypeValidation.DISABLED)
                        .subclass(targetClass);
                DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<T> definition;
                if (override) {
                    definition = builder.method(any()).intercept(InvocationHandlerAdapter.of(handler));
                } else {
                    definition = builder.method(isAbstract()).intercept(InvocationHandlerAdapter.of(handler));
                }
                // .method(isEquals()).intercept(EqualsMethod.isolated())
                // .method(isDeclaredBy(Object.class)).intercept(SuperMethodCall.INSTANCE)
                return definition.make().load(loader).getLoaded();
            }
        }, CACHE);

        // TODO cache.
        try {
            return (T) clazz.newInstance();
        } catch (final InstantiationException e) {
            throw new UndeclaredThrowableException(e);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
