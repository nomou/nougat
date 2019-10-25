package freework.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

/**
 * This class instances our own
 * <code>ParameterizedTypes</code> , <code>GenericArrayTypes</code>.
 * These are not supposed to be mixed with Java's implementations - beware of
 * equality/identity problems.
 *
 * @author vacoor
 */
public abstract class TypeFactory {

    /**
     * Creates a {@link WildcardType} instance.
     *
     * @param upperBounds the array of the upper bound(s)
     * @param lowerBounds the array of the lower bound(s)
     * @return the ParameterizedType instance
     */
    public static WildcardType createWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
        return new WildcardType() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Type[] getUpperBounds() {
                return upperBounds;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Type[] getLowerBounds() {
                return lowerBounds;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean equals(final Object that) {
                if (!(that instanceof WildcardType)) {
                    return false;
                }
                final WildcardType other = (WildcardType) that;
                return Arrays.equals(upperBounds, other.getUpperBounds()) && Arrays.equals(lowerBounds, other.getLowerBounds());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int hashCode() {
                return safeHashCode(upperBounds) ^ safeHashCode(lowerBounds);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                final StringBuilder buff = new StringBuilder();
                for (final Type upperBound : upperBounds) {
                    if (!Object.class.equals(upperBound)) {
                        buff.append(0 == buff.length() ? " extends " : " & ").append(upperBound);
                    }
                }
                int len = buff.length();
                for (final Type lowerBound : lowerBounds) {
                    buff.append(len == buff.length() ? " super " : " & ").append(lowerBound);
                }
                return buff.insert(0, "?").toString();
            }

        };
    }

    /**
     * Creates a {@link TypeVariable} instance.
     *
     * @param name        the name of TypeVariable
     * @param declaration the type declared this type variable
     * @param bounds      the array of the upper bound(s)
     * @return the ParameterizedType instance
     */
    public static <D extends GenericDeclaration> TypeVariable<D> createTypeVariable(final String name, final D declaration, final Type[] bounds) {
        return new TypeVariable<D>() {
            /**
             * {@inheritDoc}
             */
            @Override
            public Type[] getBounds() {
                return bounds;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public D getGenericDeclaration() {
                return declaration;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getName() {
                return name;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean equals(final Object that) {
                if (!(that instanceof TypeVariable<?>)) {
                    return false;
                }
                final TypeVariable<?> other = (TypeVariable<?>) that;
                return Arrays.equals(bounds, other.getBounds())
                        && safeEquals(declaration, other.getGenericDeclaration())
                        && safeEquals(name, other.getName());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int hashCode() {
                return safeHashCode(bounds) ^ safeHashCode(declaration) ^ safeHashCode(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                final StringBuilder buff = new StringBuilder();
                for (int i = 0; i < bounds.length; i++) {
                    if (!Object.class.equals(bounds[i])) {
                        buff.append(0 == buff.length() ? " extends " : " & ").append(bounds[i]);
                    }
                }
                return buff.insert(0, name).toString();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
                return null;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Annotation[] getAnnotations() {
                return new Annotation[0];
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Annotation[] getDeclaredAnnotations() {
                return new Annotation[0];
            }
        };
    }

    /**
     * Creates a {@link ParameterizedType} instance.
     *
     * @param rawType       the raw type
     * @param substTypeArgs the actual type arguments
     * @param ownerType     the type that this type is a member of
     * @return the ParameterizedType instance
     */
    public static ParameterizedType createParameterizedType(final Type rawType, final Type[] substTypeArgs, final Type ownerType) {
        return new ParameterizedType() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Type[] getActualTypeArguments() {
                return substTypeArgs;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Type getRawType() {
                return rawType;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Type getOwnerType() {
                return ownerType;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean equals(final Object that) {
                if (!(that instanceof ParameterizedType)) {
                    return false;
                }
                final ParameterizedType other = (ParameterizedType) that;
                return Arrays.equals(getActualTypeArguments(), other.getActualTypeArguments())
                        && safeEquals(getRawType(), other.getRawType())
                        && safeEquals(getOwnerType(), other.getOwnerType());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int hashCode() {
                return safeHashCode(getActualTypeArguments()) ^ safeHashCode(getRawType()) ^ safeHashCode(getOwnerType());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return toParameterizedTypeString(rawType, substTypeArgs, ownerType);
            }
        };
    }

    /**
     * Stringify {@link ParameterizedType}.
     *
     * @param rawType       the raw type
     * @param substTypeArgs the actual type arguments
     * @param ownerType     the type that this type is a member of
     * @return the ParameterizedType string
     */
    private static String toParameterizedTypeString(final Type rawType, final Type[] substTypeArgs, final Type ownerType) {
        final StringBuilder buff = new StringBuilder();
        if (null != ownerType) {
            if (ownerType instanceof Class) {
                buff.append(((Class) ownerType).getName());
            } else {
                buff.append(ownerType);
            }
            buff.append(".");
            if ((ownerType instanceof ParameterizedType)) {
                buff.append(rawType.toString().replace(ownerType.toString() + "$", ""));
            } else {
                buff.append(rawType);
            }
        } else {
            buff.append(rawType);
        }
        if (null != substTypeArgs && 0 < substTypeArgs.length) {
            buff.append("<");
            int i = 1;
            for (final Type type : substTypeArgs) {
                if (0 == i) {
                    buff.append(", ");
                }
                if ((type instanceof Class)) {
                    buff.append(((Class) type).getName());
                } else {
                    buff.append(type.toString());
                }
                i = 0;
            }
            buff.append(">");
        }
        return buff.toString();
    }

    /**
     * Creates an array type with the specified component type and length.
     *
     * @param componentType the component type
     * @return the array type instance
     */
    public static Type createArrayType(final Type componentType) {
        if (componentType instanceof Class<?>) {
            return Array.newInstance((Class<?>) componentType, 0).getClass();
        }
        return createGenericArrayType(componentType);
    }

    /**
     * Creates a {@link GenericArrayType} instance.
     *
     * @param componentType the component type
     * @return the GenericArrayType instance
     */
    public static GenericArrayType createGenericArrayType(final Type componentType) {
        return new GenericArrayType() {
            /**
             * {@inheritDoc}
             */
            @Override
            public Type getGenericComponentType() {
                return componentType;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean equals(final Object that) {
                if (!(that instanceof GenericArrayType)) {
                    return false;
                }
                final GenericArrayType other = (GenericArrayType) that;
                return safeEquals(getGenericComponentType(), other.getGenericComponentType());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public int hashCode() {
                return safeHashCode(getGenericComponentType());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "[" + getGenericComponentType() + "]";
            }
        };
    }

    /**
     * Null-safe equals.
     *
     * @param one the first object
     * @param two the second object
     * @return true if equals
     */
    private static boolean safeEquals(final Object one, final Object two) {
        return null == one ? null == two : one.equals(two);
    }

    /**
     * Null-safe hash code.
     *
     * @param one the object
     * @return the hash code
     */
    private static int safeHashCode(final Object one) {
        return null == one ? 1 : one.hashCode();
    }

    /**
     * Non-instantiate.
     */
    private TypeFactory() {
    }
}