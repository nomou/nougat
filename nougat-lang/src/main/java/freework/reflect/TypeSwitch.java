package freework.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Switch-style type judgment.
 *
 * @param <T>
 */
public class TypeSwitch<T> {

    public final T doSwitch(final Type type) {
        if (type instanceof Class<?>) {
            return caseClass((Class<?>) type);
        }
        if (type instanceof GenericArrayType) {
            return caseGenericArrayType((GenericArrayType) type);
        }
        if (type instanceof ParameterizedType) {
            return caseParameterizedType((ParameterizedType) type);
        }
        if (type instanceof TypeVariable) {
            return caseTypeVariable((TypeVariable) type);
        }
        if (type instanceof WildcardType) {
            return caseWildcardType((WildcardType) type);
        }
        return defaultCase(type);
    }

    protected T caseWildcardType(final WildcardType wildcardType) {
        return defaultCase(wildcardType);
    }

    protected T caseTypeVariable(final TypeVariable typeVariable) {
        return defaultCase(typeVariable);
    }

    protected T caseClass(final Class classType) {
        return defaultCase(classType);
    }

    protected T caseGenericArrayType(final GenericArrayType genericArrayType) {
        return defaultCase(genericArrayType);
    }

    protected T caseParameterizedType(final ParameterizedType parameterizedType) {
        return defaultCase(parameterizedType);
    }

    protected T defaultCase(final Type type) {
        return null;
    }

}