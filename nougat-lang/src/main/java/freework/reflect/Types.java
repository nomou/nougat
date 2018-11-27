/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 泛型类型参数解析工具
 * <p>
 * Java 中泛型类型局部变量的泛型参数会被擦除掉,
 * 但是类级别(静态), 实例级别的变量,方法,参数以及类定义上的类型参数都不会被擦除(匿名类属于类定义)
 *
 * @author vacoor
 */
@SuppressWarnings("unused")
public abstract class Types {
    private static final Type[] NONE_TYPES = new Type[0];

    public static boolean isClass(final Type type) {
        return type instanceof Class<?>;
    }

    public static boolean isParameterizedType(final Type type) {
        return type instanceof ParameterizedType;
    }

    public static boolean isTypeVariable(final Type type) {
        return type instanceof TypeVariable<?>;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> toClass(final Type type) {
        if (!isClass(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to Class");
        }
        return (Class<T>) type;
    }

    public static ParameterizedType toParameterizedType(final Type type) {
        if (!isParameterizedType(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to ParameterizedType");
        }
        return (ParameterizedType) type;
    }

    @SuppressWarnings("unchecked")
    public static <D extends GenericDeclaration> TypeVariable<D> toTypeVariable(final Type type) {
        if (!isTypeVariable(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to TypeVariable");
        }
        return (TypeVariable<D>) type;
    }

    /**
     * 获取 Array, Iterable 解析泛型类型参数后的元素类型, 无法解析或其他类型返回null
     *
     * @param type 需要解析的 Iterable, Array 类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Type resolveComponentType(final Type type) {
        if (isClass(type)) {
            final Class<?> clazz = toClass(type);
            if (clazz.isArray()) {
                return clazz.getComponentType();
            }
            if (Iterable.class.isAssignableFrom(clazz)) {
                return resolveActualTypeArgs(Iterable.class, (Class<? extends Iterable<?>>) clazz)[0];
            }
            return null;
        } else if (isParameterizedType(type)) {
            final ParameterizedType parameterizedType = toParameterizedType(type);
            final Type rawType = parameterizedType.getRawType();
            if (isClass(rawType)) {
                final Class<?> clazz = toClass(rawType);
                if (Iterable.class.isAssignableFrom(clazz)) {
                    return resolveActualTypeArgs(Iterable.class, (Class<? extends Iterable<?>>) clazz, parameterizedType.getActualTypeArguments())[0];
                }
            }
        }
        return null;
    }

    /**
     * 获取 type 在 TypeReference 引用的上下文中解析类型参数后的类型
     * <p>
     * //     * @param contextTypeRef 泛型类型解析上下文类型引用
     * //     * @param type           需要泛型类型参数的类型
     *
     * @return
     */
    public static <T> Type[] resolveActualTypeArgs(final Class<T> definition, final TypeReference<? extends T> contextRef) {
        final Type type = contextRef.getType();
        if (isClass(type)) {
            final Class<? extends T> clazz = toClass(type);
            return resolveActualTypeArgs(definition, clazz, clazz.getTypeParameters());
        } else if (isParameterizedType(type)) {
            final ParameterizedType parameterizedType = toParameterizedType(type);
            final Type rawType = parameterizedType.getRawType();
            final Type[] args = parameterizedType.getActualTypeArguments();

            if (!isClass(rawType)) {
                throw new IllegalArgumentException("Internal error: without actual type information");
            }
            final Class<? extends T> clazz = toClass(rawType);
            return resolveActualTypeArgs(definition, clazz, args);
        }
        throw new IllegalArgumentException("Internal error: without actual type information");
    }


    /**
     * 获取泛型超类在给定子类上下文的实际类型参数
     * <p>
     * <pre>
     *     class A&lt;V&gt; {}
     *     class B&lt;K,V&gt; extends A&lt;V&gt; {}
     *     class C extends B&lt;String, Integer&gt; {}
     *
     *     resolveActualTypeArgs(B.class, B.class.getTypeParameters(), A.class)  --&gt; V
     *     (注: 这个V定义类为B.class, 和通过A的 ParameterizedType#getActualTypeArguments() 获取到的 V 定义类为A.class, 是不同的 equals = false)
     *     resolveActualTypeArgs(C.class, C.class.getTypeParameters(), A.class)  --&gt; Integer
     *     resolveActualTypeArgs(C.class, C.class.getTypeParameters(), A.class)  --&gt; Integer
     * </pre>
     *
     * @param definition 泛型超类/接口
     * @param context    解析上下文原始类型
     * @param typeArgs   解析上下文实际类型参数
     */
    @SuppressWarnings("unchecked")
    public static <T> Type[] resolveActualTypeArgs(final Class<T> definition, final Class<? extends T> context, final Type... typeArgs) {
        /* is not class/interface. */
        if (!isInterfaceOrClass(definition) || !isInterfaceOrClass(context)) {
            throw new IllegalArgumentException("context class or generic definition superclass is only an interface or class");
        }

        /* if context not implementation / extends definition or definition not has type variables. */
        final TypeVariable<? extends Class<?>>[] typeParams = definition.getTypeParameters();
        if (!definition.isAssignableFrom(context) || 1 > typeParams.length) {
            return definition.getTypeParameters();
        }

        final Type[] finalTypeArgs = 1 > typeArgs.length ? context.getTypeParameters() : typeArgs;

        /* if context and definition is same object. */
        if (context == definition) {
            return finalTypeArgs;
        }

        final Type superType = lookupSuper(context, definition);
        if (isClass(superType)) {
            /* generic super type is class(not contains type variables). */
            return resolveActualTypeArgs(definition, (Class<? extends T>) toClass(superType), NONE_TYPES);
        } else if (isParameterizedType(superType)) {
            final ParameterizedType parameterizedSuperType = toParameterizedType(superType);
            final Type[] inTypeArgs = parameterizedSuperType.getActualTypeArguments();
            final TypeVariable<? extends Class<?>>[] typeArgNames = context.getTypeParameters();

            /* replace super type variables to actual type arguments. */
            for (int i = 0; i < inTypeArgs.length; i++) {
                final TypeVariable<?> inVar = isTypeVariable(inTypeArgs[i]) ? toTypeVariable(inTypeArgs[i]) : null;
                if (null == inVar) {
                    continue;
                }
                for (int j = 0; j < typeArgNames.length && j < finalTypeArgs.length; j++) {
                    if (inVar.equals(typeArgNames[j])) {
                        inTypeArgs[i] = finalTypeArgs[j];
                    }
                }
            }

            final Class<?> superclass = toClass(parameterizedSuperType.getRawType());
            return resolveActualTypeArgs(definition, (Class<? extends T>) superclass, inTypeArgs);
        }

        throw new UnsupportedOperationException();
    }

    private static <S> Type lookupSuper(final Class<? extends S> context, final Class<S> definition) {
        /* context is an interface or interface implementation, the superclass must be an interface. */
        final Type superType = context.getGenericSuperclass();
        final boolean isInterface = context.isInterface();
        final boolean isImpl = (null == superType || Object.class == superType || !definition.isAssignableFrom(context.getSuperclass()));

        Type target = superType;
        if (isInterface || isImpl) {
            /* lookup the implementation interface. */
            final Class<?>[] implInterfaces = context.getInterfaces();
            for (int i = 0; i < implInterfaces.length; i++) {
                if (definition.isAssignableFrom(implInterfaces[i])) {
                    target = context.getGenericInterfaces()[i];
                    break;
                }
            }
        }
        return target;
    }

    private static boolean isInterfaceOrClass(Class<?> clazz) {
        return !(clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive());
    }

    private Types() {
    }
}
