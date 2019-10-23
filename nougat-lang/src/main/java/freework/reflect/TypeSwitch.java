package freework.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Switch-style type judgment.
 *
 * @param <T> the target type
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public class TypeSwitch<T> {

    /**
     * TODO javadocs.
     */
    public final T doSwitch(final Type type) {
        if (type instanceof Class<?>) {
            return caseClass((Class<?>) type);
        }
        if (type instanceof WildcardType) {
            return caseWildcardType((WildcardType) type);
        }
        if (type instanceof TypeVariable) {
            return caseTypeVariable((TypeVariable) type);
        }
        if (type instanceof ParameterizedType) {
            return caseParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return caseGenericArrayType((GenericArrayType) type);
        }
        return defaultCase(type);
    }

    /**
     * TODO javadocs.
     */
    protected T caseClass(final Class classType) {
        return defaultCase(classType);
    }

    /**
     * TODO javadocs.
     */
    protected T caseWildcardType(final WildcardType wildcardType) {
        return defaultCase(wildcardType);
    }

    /**
     * TODO javadocs.
     */
    protected T caseTypeVariable(final TypeVariable typeVariable) {
        return defaultCase(typeVariable);
    }

    /**
     * TODO javadocs.
     */
    protected T caseParameterizedType(final ParameterizedType parameterizedType) {
        return defaultCase(parameterizedType);
    }

    /**
     * TODO javadocs.
     */
    protected T caseGenericArrayType(final GenericArrayType genericArrayType) {
        return defaultCase(genericArrayType);
    }

    /**
     * TODO javadocs.
     */
    protected T defaultCase(final Type type) {
        return null;
    }
}