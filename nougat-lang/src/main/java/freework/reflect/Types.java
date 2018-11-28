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
 * Reflect types utils.
 * <p>
 * NOTE:Generic type definition will erase at compiling in java, but the class and class member is not erase.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("unused")
public abstract class Types {
    /**
     * Non-Types array.
     */
    private static final Type[] NONE_TYPES = new Type[0];

    /**
     * Non-instantiate.
     */
    private Types() {
    }

    /**
     * Returns whether the given type is a Class object.
     *
     * @param type the type
     * @return true if the type is a Class object
     */
    public static boolean isClass(final Type type) {
        return type instanceof Class<?>;
    }

    /**
     * Returns whether the given type is a ParameterizedType object.
     *
     * @param type the type
     * @return true if the type is a ParameterizedType object
     */
    public static boolean isParameterizedType(final Type type) {
        return type instanceof ParameterizedType;
    }

    /**
     * Returns whether the given type is a TypeVariable object.
     *
     * @param type the type
     * @return true if the type is a TypeVariable object
     */
    public static boolean isTypeVariable(final Type type) {
        return type instanceof TypeVariable<?>;
    }

    /**
     * Returns the Class object if the given type is a Class object.
     *
     * @param type the type
     * @param <T>  the class type
     * @return the class object
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toClass(final Type type) {
        if (!isClass(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to Class");
        }
        return (Class<T>) type;
    }

    /**
     * Returns the ParameterizedType object if the given type is a ParameterizedType object.
     *
     * @param type the type
     * @return the ParameterizedType object
     */
    public static ParameterizedType toParameterizedType(final Type type) {
        if (!isParameterizedType(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to ParameterizedType");
        }
        return (ParameterizedType) type;
    }

    /**
     * Returns the TypeVariable object if the given type is a TypeVariable object.
     *
     * @param type the type
     * @return the TypeVariable object
     */
    @SuppressWarnings("unchecked")
    public static <D extends GenericDeclaration> TypeVariable<D> toTypeVariable(final Type type) {
        if (!isTypeVariable(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to TypeVariable");
        }
        return (TypeVariable<D>) type;
    }

    /**
     * Gets the component type of the array/java.util.Iterable type.
     *
     * @param type the array/java.util.Iterable type
     * @return the component type if resolved, otherwise null
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
     * Gets the actual parameterized type of the generic class definition in context.
     *
     * @param definition the generic class definition
     * @param contextRef the context reference
     * @return the actual paramerized type
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
     * Gets the actual parameterized type of the generic class definition in context.
     * <p>
     * <code>
     * <pre>
     *     class A&lt;V&gt; {}
     *     class B&lt;K,V&gt; extends A&lt;V&gt; {}
     *     class C extends B&lt;String, Integer&gt; {}
     *
     *     resolveActualTypeArgs(B.class, B.class.getTypeParameters(), A.class)  --&gt; V
     *     resolveActualTypeArgs(C.class, C.class.getTypeParameters(), A.class)  --&gt; Integer
     *     resolveActualTypeArgs(C.class, C.class.getTypeParameters(), A.class)  --&gt; Integer
     * </pre>
     * </code>
     *
     * @param definition the generic class definition
     * @param context    the context reference
     * @param typeArgs   the context type variable
     * @return the actual paramerized type
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

    /**
     * TODO javadocs.
     */
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

    /**
     * Returns whether the given class is a class or interface(not annotation/array/enum/primitive).
     *
     * @param clazz the class
     * @return true if class is not annotation/array/enum/primitive, otherwise false
     */
    private static boolean isInterfaceOrClass(final Class<?> clazz) {
        return !(clazz.isAnnotation() || clazz.isArray() || clazz.isEnum() || clazz.isPrimitive());
    }

}
