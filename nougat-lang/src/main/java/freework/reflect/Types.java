/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.reflect;

import java.lang.reflect.*;
import java.util.Arrays;

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
     * Gets the component type of the array/java.util.Iterable type (use type variable bound as type if type is type variable).
     *
     * @param type        the array/{@link java.lang.Iterable} type
     * @param runtimeType the array/java.util.Iterable context type
     * @return the component type if resolved, otherwise null
     */
    @SuppressWarnings("unchecked")
    public static Type resolveComponentType(final Type type, final Type runtimeType) {
        return resolveComponentType(type, runtimeType, true);
    }

    /**
     * Gets the component type of the array/java.util.Iterable type.
     *
     * @param type        the array/{@link java.lang.Iterable} type
     * @param runtimeType the array/java.util.Iterable context type
     * @param boundAsType use type variable bound as component type
     * @return the component type if resolved, otherwise null
     */
    public static Type resolveComponentType(final Type type, final Type runtimeType, final boolean boundAsType) {
        final Type resolvedType = resolveType(type, runtimeType, boundAsType);
        if (isClass(resolvedType)) {
            final Class<?> clazz = toClass(resolvedType);
            if (clazz.isArray()) {
                return clazz.getComponentType();
            }
            if (Iterable.class.isAssignableFrom(clazz)) {
                return resolveType(Iterable.class.getTypeParameters()[0], clazz, boundAsType);
            }
            return null;
        } else if (isParameterizedType(resolvedType)) {
            final Class<?> clazz = getRawClass(toParameterizedType(resolvedType));
            if (Iterable.class.isAssignableFrom(clazz)) {
                return resolveType(Iterable.class.getTypeParameters()[0], resolvedType, boundAsType);
            }
        } else if (isGenericArrayType(resolvedType)) {
            return toGenericArrayType(resolvedType).getGenericComponentType();
        }
        return null;
    }

    /**
     * Gets the resolved type (use type variable bound as type if type is type variable).
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
        return resolveType(type, runtimeType, true);
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
     * @param boundAsType use type variable bound as component type
     * @return the resolved type
     */
    public static Type resolveType(final Type type, final Type runtimeType, final boolean boundAsType) {
        Type ret = type;
        if (isWildcardType(type)) {
            ret = resolveWildcardType(toWildcardType(type), runtimeType, boundAsType);
        } else if (isTypeVariable(type)) {
            ret = resolveTypeVariable(toTypeVariable(type), runtimeType, boundAsType);
        } else if (isParameterizedType(type)) {
            ret = resolveParameterizedType(toParameterizedType(type), runtimeType, boundAsType);
        } else if (isGenericArrayType(type)) {
            ret = resolveGenericArrayType(toGenericArrayType(type), runtimeType, boundAsType);
        }
        if (ret instanceof TypeVariable) {
            final TypeVariable<GenericDeclaration> typeVariable = toTypeVariable(ret);
            if (boundAsType) {
                ret = boundAsType(typeVariable);
            }
            /* keep the outermost type
            final Type[] bounds = typeVariable.getBounds();
            boolean resolved = false;
            for (int i = 0; i < bounds.length; i++) {
                final Type boundType = bounds[i];
                bounds[i] = boundAsType(boundType);
                resolved = resolved || boundType.equals(bounds[i]);
            }
            if (resolved) {
                ret = TypeFactory.createTypeVariable(typeVariable.getName(), typeVariable.getGenericDeclaration(), bounds);
            }
            */
        }
        return ret;
    }

    private static Type boundAsType(final Type bound) {
        if (isTypeVariable(bound)) {
            final TypeVariable<GenericDeclaration> typeVariable = toTypeVariable(bound);
            final Type[] bounds = typeVariable.getBounds();
            if (1 == bounds.length) {
                return boundAsType(bounds[0]);
            }
        }
        return bound;
    }

    private static Type resolveWildcardType(final WildcardType type, final Type runtimeType, final boolean boundAsType) {
        final Type[] lowerBounds = type.getLowerBounds();
        final Type[] upperBounds = type.getUpperBounds();
        final boolean resolved = doResolve(lowerBounds, runtimeType, boundAsType) || doResolve(upperBounds, runtimeType, boundAsType);
        return resolved ? TypeFactory.createWildcardType(upperBounds, lowerBounds) : type;
    }

    private static Type resolveTypeVariable(final TypeVariable<? extends GenericDeclaration> typeVariable, final Type runtimeType, final boolean boundAsType) {
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
        } else if (declaration instanceof Method) {
            final Type[] bounds = typeVariable.getBounds();
            final boolean resolved = doResolve(bounds, runtimeType, boundAsType);
            /*-
            // bound as type ?
            if (resolved && 1 == bounds.length) {
                return bounds[0];
            }
            */
            return resolved ? TypeFactory.createTypeVariable(typeVariable.getName(), declaration, bounds) : typeVariable;
        }
        return typeVariable;
    }

    private static boolean doResolve(final Type[] types, final Type runtimeType, final boolean boundAsType) {
        boolean resolved = false;
        for (int i = 0; i < types.length; i++) {
            final Type type = types[i];
            types[i] = resolveType(type, runtimeType, boundAsType);
            resolved = resolved || !type.equals(types[i]);
        }
        return resolved;
    }

    private static Type resolveParameterizedType(final ParameterizedType type, final Type runtimeType, final boolean boundAsType) {
        final Type[] typeArguments = type.getActualTypeArguments();
        final boolean resolved = doResolve(typeArguments, runtimeType, boundAsType);
        return !resolved ? type : TypeFactory.createParameterizedType(type.getRawType(), typeArguments, null);
    }

    private static Type resolveGenericArrayType(final GenericArrayType type, final Type runtimeType, final boolean boundAsType) {
        final Type componentType = type.getGenericComponentType();
        final Type resolvedType = resolveType(componentType, runtimeType, boundAsType);
        return !componentType.equals(resolvedType) ? TypeFactory.createGenericArrayType(resolvedType) : type;
    }

    private static Type matches(final TypeVariable<?> typeVariable, final Class<?> declaringClass, final Type[] resolvedTypeArguments) {
        final TypeVariable<? extends Class<?>>[] declaredTypeVariables = declaringClass.getTypeParameters();
        if (declaredTypeVariables.length != resolvedTypeArguments.length) {
            throw new IllegalArgumentException(String.format("declared type variables length %s != resolved type arguments length %s on declaring class: %s", Arrays.toString(declaredTypeVariables), Arrays.toString(resolvedTypeArguments), declaringClass));
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

    public static boolean canAccept(final Type expectedType, final Type actualType) {
        return expectedType.equals(actualType) || new TypeSwitch<Boolean>() {
            @Override
            protected Boolean caseClass(final Class classType) {
                return isClass(actualType) && classType.isAssignableFrom(Types.toClass(actualType));
            }

            @Override
            protected Boolean caseWildcardType(final WildcardType wildcardType) {
                Type[] actualUpperBounds;
                Type[] actualLowerBounds;
                if (isClass(actualType)) {
                    actualUpperBounds = new Type[]{toClass(actualType)};
                    actualLowerBounds = actualUpperBounds;
                } else if (isWildcardType(actualType)) {
                    final WildcardType actualWildcardType = toWildcardType(actualType);
                    actualUpperBounds = actualWildcardType.getUpperBounds();
                    actualLowerBounds = actualWildcardType.getLowerBounds();
                } else {
                    actualUpperBounds = new Type[]{Object.class};
                    actualLowerBounds = actualUpperBounds;
                }

                final Type[] expectedUpperBounds = wildcardType.getUpperBounds();
                final Type[] expectedLowerBounds = wildcardType.getLowerBounds();

                for (final Type expectedUpperBound : expectedUpperBounds) {
                    for (final Type actualUpperBound : actualUpperBounds) {
                        if (!canAccept(expectedUpperBound, actualUpperBound)) {
                            return false;
                        }
                    }
                }
                for (final Type lowerBound : expectedLowerBounds) {
                    for (final Type actualLowerBound : actualLowerBounds) {
                        if (!canAccept(actualLowerBound, lowerBound)) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            protected Boolean caseParameterizedType(final ParameterizedType parameterizedType) {
                final Type expectedRawType = parameterizedType.getRawType();
                final Type[] expectedTypeArgs = parameterizedType.getActualTypeArguments();
                if (isClass(expectedRawType)) {
                    final Class<?> parentClass = toClass(expectedRawType);
                    final TypeVariable<? extends Class<?>>[] typeArgs = parentClass.getTypeParameters();

                    Class<?> actualRawClass = null;
                    if (isClass(actualType)) {
                        actualRawClass = toClass(actualType);
                    } else if (isParameterizedType(actualType)) {
                        final ParameterizedType actualParameterizedType = toParameterizedType(actualType);
                        final Type actualRawType = actualParameterizedType.getRawType();
                        actualRawClass = isClass(actualRawType) ? toClass(actualRawType) : null;
                    }
                    if (null != actualRawClass && parentClass.isAssignableFrom(actualRawClass)) {
                        for (int index = 0; index < typeArgs.length; index++) {
                            if (!canAccept(expectedTypeArgs[index], resolveType(typeArgs[index], actualType, false))) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected Boolean caseGenericArrayType(final GenericArrayType genericArrayType) {
                final Type expectedComponentType = genericArrayType.getGenericComponentType();
                if (isClass(actualType)) {
                    final Class<?> actualClass = toClass(actualType);
                    if (actualClass.isArray()) {
                        return canAccept(expectedComponentType, actualClass.getComponentType());
                    }
                } else if (isGenericArrayType(actualType)) {
                    final GenericArrayType actualArrayType = toGenericArrayType(actualType);
                    return canAccept(expectedComponentType, actualArrayType.getGenericComponentType());
                }
                return false;
            }

            @Override
            protected Boolean defaultCase(final Type type) {
                return type.equals(actualType);
            }
        }.doSwitch(expectedType);
    }
}
