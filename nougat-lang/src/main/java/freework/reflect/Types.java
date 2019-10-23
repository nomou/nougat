/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Utilities of Type.
 * <p>
 * NOTE:Generic type definition will erase at compiling in java, but the class and class member is not erase.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
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
     * Returns whether the given type is a WildcardType object.
     *
     * @param type the type
     * @return true if the type is a WildcardType object
     */
    public static boolean isWildcardType(final Type type) {
        return type instanceof WildcardType;
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
     * Returns whether the given type is a ParameterizedType object.
     *
     * @param type the type
     * @return true if the type is a ParameterizedType object
     */
    public static boolean isParameterizedType(final Type type) {
        return type instanceof ParameterizedType;
    }

    /**
     * Returns whether the given type is a GenericArrayType object.
     *
     * @param type the type
     * @return true if the type is a GenericArrayType object
     */
    public static boolean isGenericArrayType(final Type type) {
        return type instanceof GenericArrayType;
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
     * Returns the WildcardType object if the given type is a WildcardType object.
     *
     * @param type the type
     * @return the WildcardType object
     */
    public static WildcardType toWildcardType(final Type type) {
        if (!isWildcardType(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to WildcardType");
        }
        return (WildcardType) type;
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
     * Returns the GenericArrayType object if the given type is a GenericArrayType object.
     *
     * @param type the type
     * @return the GenericArrayType object
     */
    public static GenericArrayType toGenericArrayType(final Type type) {
        if (!isGenericArrayType(type)) {
            throw new IllegalArgumentException("type " + type + " cannot cast to ParameterizedType");
        }
        return (GenericArrayType) type;
    }

    /**
     * Gets the component type of the array/java.util.Iterable type.
     *
     * @param type the array/{@link java.lang.Iterable} type
     * @return the component type if resolved, otherwise null
     */
    @SuppressWarnings("unchecked")
    public static Type resolveComponentType(final Type type, final Type runtimeType) {
        final Type resolvedType = resolveType(type, runtimeType);
        if (isClass(resolvedType)) {
            final Class<?> clazz = toClass(resolvedType);
            if (clazz.isArray()) {
                return clazz.getComponentType();
            }
            if (Iterable.class.isAssignableFrom(clazz)) {
                return resolveType(Iterable.class.getTypeParameters()[0], clazz);
            }
            return null;
        } else if (isParameterizedType(resolvedType)) {
            final Class<?> clazz = getRawClass(toParameterizedType(resolvedType));
            if (Iterable.class.isAssignableFrom(clazz)) {
                return resolveType(Iterable.class.getTypeParameters()[0], resolvedType);
            }
        } else if (isGenericArrayType(resolvedType)) {
            return toGenericArrayType(resolvedType).getGenericComponentType();
        }
        return null;
    }

    /**
     * Gets the resolved type.
     * <p>
     * Examples:
     * <ul>
     * <li>Gets the iterable generic type variable: Types.resolveType(Iterable.class.getTypeParameters()[0], resolvedType);</li>
     * <li>Gets the resolved method generic return type: Types.resolveType(Method.getGenericReturnType(), clazz);</li>
     * </ul>
     * </p>
     *
     * @param type        the type
     * @param runtimeType the type arguments context
     * @return the resolved type
     */
    public static Type resolveType(final Type type, final Type runtimeType) {
        if (isWildcardType(type)) {
            return resolveWildcardType(toWildcardType(type), runtimeType);
        } else if (isTypeVariable(type)) {
            return resolveTypeVariable(toTypeVariable(type), runtimeType);
        } else if (isParameterizedType(type)) {
            return resolveParameterizedType(toParameterizedType(type), runtimeType);
        } else if (isGenericArrayType(type)) {
            return resolveGenericArrayType(toGenericArrayType(type), runtimeType);
        }
        return type;
    }

    private static Type resolveWildcardType(final WildcardType type, final Type runtimeType) {
        final Type[] lowerBounds = type.getLowerBounds();
        final Type[] upperBounds = type.getUpperBounds();

        for (int i = 0; i < lowerBounds.length; i++) {
            lowerBounds[i] = resolveType(lowerBounds[i], runtimeType);
        }
        for (int i = 0; i < upperBounds.length; i++) {
            upperBounds[i] = resolveType(upperBounds[i], runtimeType);
        }
        return TypeFactory.createWildcardType(upperBounds, lowerBounds);
    }

    private static Type resolveTypeVariable(final TypeVariable<? extends GenericDeclaration> typeVariable, final Type runtimeType) {
        final GenericDeclaration declaration = typeVariable.getGenericDeclaration();
        // XXX: Only resolve type variables defined on the class
        if (declaration instanceof Class<?>) {
            final Class<?> declaringClass = (Class<?>) declaration;

            Class<?> runtimeClass;
            if (isClass(runtimeType)) {
                runtimeClass = toClass(runtimeType);
            } else if (isParameterizedType(runtimeType)) {
                runtimeClass = getRawClass(toParameterizedType(runtimeType));
            } else {
                throw new IllegalArgumentException("The 'runtimeType' arg must be Class or ParameterizedType, but was: " + runtimeType.getClass());
            }

            if (!declaringClass.isAssignableFrom(runtimeClass)) {
                throw new IllegalArgumentException("The 'runtimeTime' must be generic declaration");
            }

            final Type[] typeArguments = resolveTypeVariables(runtimeType, declaringClass);
            return matches(typeVariable, declaringClass, typeArguments);
        }
        return typeVariable;
    }

    private static Type resolveParameterizedType(final ParameterizedType type, final Type runtimeType) {
        boolean resolved = false;
        final Type[] typeArguments = type.getActualTypeArguments();
        for (int i = 0; i < typeArguments.length; i++) {
            final Type typeArgument = typeArguments[i];
            typeArguments[i] = resolveType(typeArgument, runtimeType);
            resolved = resolved || typeArgument != typeArguments[i];
        }
        return !resolved ? type : TypeFactory.createParameterizedType(type.getRawType(), typeArguments, null);
    }

    private static Type resolveGenericArrayType(final GenericArrayType type, final Type runtimeType) {
        final Type componentType = type.getGenericComponentType();
        final Type resolvedType = resolveType(componentType, runtimeType);
        return componentType == resolvedType ? type : TypeFactory.createGenericArrayType(resolvedType);
    }

    private static Type matches(final TypeVariable<?> typeVariable, final Class<?> declaringClass, final Type[] resolvedTypeArguments) {
        final TypeVariable<? extends Class<?>>[] declaredTypeVariables = declaringClass.getTypeParameters();
        if (declaredTypeVariables.length != resolvedTypeArguments.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < declaredTypeVariables.length; i++) {
            if (declaredTypeVariables[i] == typeVariable) {
                return resolvedTypeArguments[i];
            }
        }
        throw new IllegalArgumentException("type variable not declared in " + declaringClass);
    }

    private static Type[] resolveTypeVariables(final Type runtimeType, final Class<?> declaringClass) {
        if (declaringClass.isAnnotation() || declaringClass.isArray() || declaringClass.isEnum() || declaringClass.isPrimitive()) {
            throw new IllegalArgumentException("declaring class must be interface or class");
        }

        Class<?> runtimeClass;
        if (isClass(runtimeType)) {
            runtimeClass = toClass(runtimeType);
        } else if (isParameterizedType(runtimeType)) {
            runtimeClass = getRawClass(toParameterizedType(runtimeType));
        } else {
            throw new IllegalArgumentException("The 1nd arg must be Class or ParameterizedType, but was: " + runtimeType.getClass());
        }

        if (runtimeClass.equals(declaringClass)) {
            return isClass(runtimeType) ? runtimeClass.getTypeParameters() : toParameterizedType(runtimeType).getActualTypeArguments();
        }

        final Type superclass = lookupSuperclass(runtimeClass, declaringClass);
        if (isParameterizedType(superclass)) {
            final ParameterizedType superParameterizedType = toParameterizedType(superclass);
            final Type[] typeArguments = superParameterizedType.getActualTypeArguments();
            for (int i = 0; i < typeArguments.length; i++) {
                if (isTypeVariable(typeArguments[i])) {
                    final TypeVariable<? extends Class<?>>[] declaredTypeVariables = runtimeClass.getTypeParameters();
                    for (int j = 0; j < declaredTypeVariables.length; j++) {
                        if (declaredTypeVariables[j] == typeArguments[i]) {
                            if (isParameterizedType(runtimeType)) {
                                typeArguments[i] = toParameterizedType(runtimeType).getActualTypeArguments()[j];
                            } else {
                                typeArguments[i] = declaredTypeVariables[i];
                            }
                        }
                    }
                }
            }
            return typeArguments;
        }
        return resolveTypeVariables(superclass, declaringClass);
    }

    /**
     * TODO javadocs.
     */
    private static Type lookupSuperclass(final Class<?> runtimeClass, final Class<?> declaringClass) {
        final Class<?> superclass = runtimeClass.getSuperclass();
        if (null != superclass && declaringClass.isAssignableFrom(superclass)) {
            return runtimeClass.getGenericSuperclass();
        }

        final Type[] interfaces = runtimeClass.getGenericInterfaces();
        for (final Type interfaceType : interfaces) {
            Class<?> interfaceClass;
            if (isClass(interfaceType)) {
                interfaceClass = toClass(interfaceType);
            } else if (isParameterizedType(interfaceType)) {
                interfaceClass = getRawClass((toParameterizedType(interfaceType)));
            } else {
                throw new IllegalStateException("interface should be Class or ParameterizedType, but was: " + interfaceType.getClass());
            }
            if (declaringClass.isAssignableFrom(interfaceClass)) {
                return interfaceType;
            }
        }
        throw new IllegalStateException("runtimeClass '" + runtimeClass + "' not instanceof '" + declaringClass + "' ?");
    }

    private static Class<?> getRawClass(final ParameterizedType parameterizedType) {
        return toClass(parameterizedType.getRawType());
    }
}
