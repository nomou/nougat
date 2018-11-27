package freework.reflect;

import freework.util.Primitives;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection utils.
 *
 * @author vacoor
 * @since 1.0
 */
public class Reflect {
    /**
     * wrapped object.
     */
    private final Object target;

    /**
     * Creates a reflect instance.
     *
     * @param target wrapped object
     */
    private Reflect(final Object target) {
        this.target = target;
    }

    /**
     * Gets the wrapped object.
     *
     * @param <T> wrapped object type
     * @return wraped object
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) target;
    }

    /**
     * Returns true if internal object is present, otherwise false.
     *
     * @return true if internal object is present, otherwise false.
     */
    public boolean isPresent() {
        return null != get();
    }

    /* *****************************
     *         Property Methods
     * *************************** */

    /**
     * Gets the given property value.
     * <p>
     * The property values are obtained by the getter.
     *
     * @param property the name of property
     * @return the reflect for property value
     */
    public Reflect property(final String property) {
        if (null == property || 1 > property.length()) {
            throw new IllegalArgumentException("illegal property name: " + property);
        }
        final String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        Object ret;
        try {
            ret = this.call("get" + suffix);
        } catch (final IllegalStateException ignore) {
            try {
                ret = this.call("is" + suffix);
            } catch (final IllegalStateException ignore2) {
                throw new IllegalStateException("Method not found: get" + suffix + "/is" + suffix + " in " + getType());
            }
        }
        return wrap(ret);
    }

    /**
     * Gets the value of given property.
     * <p>
     * The Property values are set by setter.
     *
     * @param property the name of property
     * @param value    the value of property
     * @return this reflect instance
     */
    public Reflect property(final String property, final Object value) {
        if (null == property || 1 > property.length()) {
            throw new IllegalArgumentException("illegal property name: " + property);
        }

        final String method = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        this.call(method, value);
        return this;
    }

    /* *****************************
     *         Field Methods
     * *************************** */

    /**
     * Gets the value of given field.
     *
     * @param field the name of field
     * @return the reflect for field value
     */
    public Reflect get(final String field) {
        final Reflect f = this.field(field);
        if (!f.isPresent()) {
            throw new IllegalStateException("Field not found: " + field + " in " + getType());
        }
        try {
            return wrap(f.<Field>get().get(target));
        } catch (final IllegalAccessException ex) {
            return handleReflectionException(ex);
        }
    }

    /**
     * Sets the value of field.
     *
     * @param field the name of field
     * @param value the value of field
     * @return this reflect instance
     */
    public Reflect set(final String field, final Object value) {
        final Reflect f = this.field(field);
        if (!f.isPresent()) {
            throw new IllegalStateException("Field not found: " + field + field + " in " + getType());
        }
        try {
            (f.<Field>get()).set(target, value);
        } catch (final IllegalAccessException ex) {
            handleReflectionException(ex);
        }
        return this;
    }

    /* *******************************
     *        Array/List Methods
     * ***************************** */

    /**
     * Gets the value of array/list.
     *
     * @param index the index of the array/list
     * @return the reflect for element at the specified position in the array/list
     */
    public Reflect get(final int index) {
        if (!this.isPresent()) {
            throw new IllegalStateException("is not present");
        }
        Object ret = null;
        if (null != target) {
            if (this.getType().isArray()) {
                final int len = Array.getLength(target);
                ret = index < len ? Array.get(target, index) : null;
            } else if (target instanceof List<?>) {
                final List<?> seq = (List<?>) target;
                ret = index < seq.size() ? seq.get(index) : null;
            } else {
                throw new IllegalStateException("target is not sequence: " + target);
            }
        }
        return wrap(ret);
    }

    /**
     * Sets the value of array/list.
     *
     * @param index the index of the array/list
     * @param value the element at the specified position in the array/list
     * @return this reflect instance
     */
    @SuppressWarnings("unchecked")
    public Reflect set(final int index, final Object value) {
        if (this.getType().isArray()) {
            Array.set(target, index, value);
        } else if (target instanceof List<?>) {
            final List<Object> seq = (List<Object>) target;
            for (int i = seq.size() - 1; i <= index; i++) {
                seq.add(null);
            }
            seq.set(index, value);
        } else {
            throw new IllegalStateException("target is not array/list: " + target);
        }
        return this;
    }

    /**
     * Instantiate a new-instance for target.
     *
     * @param args the constructor args
     * @return the reflect of new-instance
     */
    public Reflect instantiate(final Object... args) {
        Object instance;
        if (target instanceof Constructor<?>) {
            instance = doInvoke((Constructor<?>) target, args);
        } else {
            final Reflect ctor = this.constructor(typeof(args));
            if (!ctor.isPresent()) {
                throw new IllegalStateException("target is not present: " + target);
            }
            instance = doInvoke(ctor.<Constructor<?>>get(), args);
        }
        return wrap(instance);
    }

    /**
     * Calls the specified method.
     *
     * @param method the name of method
     * @param args   the arguments for method
     * @return the reflect for return value of method
     */
    public Reflect call(final String method, final Object... args) {
        return this.call(method, typeof(args), args);
    }

    /**
     * Gets the type array of given arguments array.
     *
     * @param args the arguments array
     * @return the type array
     */
    private Class<?>[] typeof(final Object... args) {
        if (null == args) {
            return new Class<?>[0];
        }

        final Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = null != args[i] ? args[i].getClass() : null;
        }
        return types;
    }

    /**
     * Calls the specified method.
     *
     * @param method        the name of method
     * @param applyArgTypes the actual types of parameters
     * @param args          the arguments for method
     * @return the reflect for return value of method
     */
    public Reflect call(final String method, final Class<?>[] applyArgTypes, final Object[] args) {
        final Reflect invoker = this.method(method, applyArgTypes);
        if (!invoker.isPresent()) {
            throw new IllegalStateException("Method not found: " + method);
        }
        return wrap(doInvoke(invoker.<Method>get(), target, args));
    }

    /* ********************************
     *          Member Matches
     * ****************************** */

    /**
     * Gets the best match constructor for the given actual parameter types.
     *
     * @param applyArgTypes the parameter types of constructor
     * @return the reflect for best match result
     */
    public Reflect constructor(final Class<?>... applyArgTypes) {
        if (!isPresent()) {
            throw new IllegalStateException("this target is not present");
        }

        Constructor<?> ctor;
        try {
            ctor = constructor(getType(), applyArgTypes);
        } catch (final Exception ex) {
            ctor = null;
        }
        return wrap(ctor);
    }

    /**
     * Gets the field of wrapped object.
     *
     * @return the reflect for field
     */
    public Reflect field(final String field) {
        if (!isPresent()) {
            throw new IllegalStateException("this is not present");
        }

        Field f;
        try {
            f = field(getType(), field);
        } catch (final Exception ex) {
            f = null;
        }
        return wrap(f);
    }

    /**
     * Gets the best match method for the given method name and actual parameter types.
     *
     * @param method        the name of method
     * @param applyArgTypes the parameter types of method
     * @return the reflect for best match result
     */
    public Reflect method(final String method, final Class<?>... applyArgTypes) {
        if (!isPresent()) {
            throw new IllegalStateException("this is not present");
        }

        Method invoker;
        try {
            invoker = method(getType(), method, applyArgTypes);
        } catch (final Exception e) {
            invoker = null;
        }
        return wrap(invoker);
    }

    /**
     * Gets the wrapped target class.
     *
     * @return return target if target is class, otherwise target.getClass()
     */
    private Class<?> getType() {
        return (null == target || target instanceof Class<?>) ? (Class<?>) target : target.getClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{ Reflect: " + target + " }";
    }


    /* ****************************************
     *            STATIC METHOD
     * ************************************** */

    /**
     * Returns a reflect object for the specified target object.
     *
     * @param target the target object
     * @return the reflect for target
     */
    public static Reflect wrap(final Object target) {
        return new Reflect(target);
    }

    /**
     * Sets the {@code accessible} flag for this object to true,  A value of {@code true} indicates that the reflected
     * object should suppress Java language access checking when it is used.
     *
     * @param accessible {@link AccessibleObject}
     * @param <T>        {@link AccessibleObject} type
     * @return the accessible object
     */
    public static <T extends AccessibleObject> T accessible(final T accessible) {
        if (null == accessible) {
            return null;
        }
        if (accessible instanceof Member) {
            final Member member = (Member) accessible;
            if (Modifier.isPublic(member.getModifiers()) &&
                    Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                return accessible;
            }
        }
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }
        return accessible;
    }

    /* *****************************************
     *              FIELD MATCHES
     * *************************************** */

    /**
     * Gets the field of the specified class, lookup in the following order:
     * <ol>
     * <li>public field</li>
     * <li>declared field</li>
     * <li>ancestors declared field</li>
     * </ol>
     *
     * @param clazz the target class
     * @param name  the name of field
     * @return the field object
     */
    private static Field field(Class<?> clazz, final String name) {
        NoSuchFieldException exception = null;
        try {
            // try get public field.
            return clazz.getField(name);
        } catch (final NoSuchFieldException e) {
            do {
                try {
                    return accessible(clazz.getDeclaredField(name));
                } catch (NoSuchFieldException e1) {
                    exception = null != exception ? exception : e1;
                }
                clazz = clazz.getSuperclass();
            } while (null != clazz);
        }
        return handleReflectionException(exception);
    }

    /* *****************************************
     *        CONSTRUCTOR/METHOD MATCHES
     * *************************************** */

    /**
     * Gets the best match constructor.
     *
     * @param clazz         the class
     * @param argumentTypes the argument types
     * @return the best matched constructor
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> constructor(final Class<?> clazz, final Class<?>... argumentTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(argumentTypes);
        } catch (final NoSuchMethodException ex) { /* ignore */ }

        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        try {
            return constructor(constructors, argumentTypes);
        } catch (final NoSuchMethodException e) {
            try {
                constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
                return accessible(constructor(constructors, argumentTypes));
            } catch (final NoSuchMethodException ex) {
                return handleReflectionException(ex);
            }
        }
    }

    /**
     * Gets the best match method.
     *
     * @param clazz         the class declaring this method
     * @param method        the name of method
     * @param argumentTypes the argument types
     * @return the best matched method
     */
    private static Method method(Class<?> clazz, final String method, final Class<?>... argumentTypes) {
        NoSuchMethodException exception = null;
        try {
            return clazz.getMethod(method, argumentTypes);
        } catch (NoSuchMethodException ex) { /* ignore */ }

        Method[] methods = clazz.getMethods();
        try {
            return method(methods, method, argumentTypes);
        } catch (NoSuchMethodException e) {
            do {
                try {
                    methods = clazz.getDeclaredMethods();
                    return accessible(method(methods, method, argumentTypes));
                } catch (NoSuchMethodException e1) {
                    if (null == exception) {
                        exception = e1;
                    }
                }

                clazz = clazz.getSuperclass();
            } while (null != clazz);
        }
        return handleReflectionException(exception);
    }

    /**
     * Gets the best match constructor.
     *
     * @param candidates    the constructor matches list
     * @param argumentTypes the argument types
     * @return the matched method
     * @throws NoSuchMethodException if not unique constructor found
     */
    private static <T> Constructor<T> constructor(final Constructor<T>[] candidates, final Class<?>... argumentTypes)
            throws NoSuchMethodException {
        final List<Constructor<T>> conflicts = new ArrayList<Constructor<T>>(candidates.length);

        Constructor<T> matched = null;
        Class<?>[] matchedParameterTypes = null;
        for (final Constructor<T> ctor : candidates) {
            Class[] declaredTypes = ctor.getParameterTypes();
            if (isCompatible(declaredTypes, argumentTypes, false)) {
                // the declared types is compatible with arguments types.
                return ctor;
            }

            // adjust varargs types length to given length, varargs may not exist.
            declaredTypes = ctor.isVarArgs() ? adjustVarArgsTypesTo(declaredTypes, argumentTypes.length) : declaredTypes;
            if (!isCompatible(declaredTypes, argumentTypes, true)) {
                // the declared types is not compatible with arguments types
                continue;
            }

            final int matches = matchesCompare(matched, matchedParameterTypes, ctor, declaredTypes, argumentTypes);
            if (1 == matches) {
                conflicts.clear();
                matched = ctor;
                matchedParameterTypes = declaredTypes;
            } else if (0 == matches) {
                conflicts.add(ctor);
            }
        }

        if (!conflicts.isEmpty()) {
            conflicts.add(0, matched);
            throw new NoSuchMethodException("Ambiguous constructors are found. " + conflicts);
        }

        if (null == matched) {
            throw new NoSuchMethodException("Constructor is not found ");
        }

        return matched;
    }

    /**
     * Gets the best match method.
     *
     * @param candidates    the methods matches list
     * @param method        the name of method
     * @param argumentTypes the argument types
     * @return the matched method
     * @throws NoSuchMethodException if not unique method found
     */
    private static Method method(final Method[] candidates, final String method, final Class<?>... argumentTypes)
            throws NoSuchMethodException {
        final List<Method> conflicts = new ArrayList<Method>(candidates.length);

        Method matched = null;
        Class<?>[] matchedParameterTypes = null;
        for (final Method candidate : candidates) {
            if (!candidate.getName().equals(method)) {
                continue;
            }

            Class<?>[] declaredTypes = candidate.getParameterTypes();
            if (isCompatible(declaredTypes, argumentTypes, false)) {
                // the declared types is compatible with arguments types.
                return candidate;
            }

            // adjust varargs types length to given length, varargs may not exist.
            declaredTypes = candidate.isVarArgs() ? adjustVarArgsTypesTo(declaredTypes, argumentTypes.length) : declaredTypes;
            if (!isCompatible(declaredTypes, argumentTypes, true)) {
                // the declared types is not compatible with arguments types
                continue;
            }

            final int matches = matchesCompare(matched, matchedParameterTypes, candidate, declaredTypes, argumentTypes);
            if (1 == matches) {
                conflicts.clear();
                matched = candidate;
                matchedParameterTypes = declaredTypes;
            } else if (0 == matches) {
                conflicts.add(candidate);
            }
        }

        if (!conflicts.isEmpty()) {
            conflicts.add(0, matched);
            throw new NoSuchMethodException("Ambiguous methods are found. " + conflicts);
        }
        if (null == matched) {
            throw new NoSuchMethodException("Method is not found: " + method + '(' + Arrays.toString(argumentTypes) + ')');
        }
        return matched;
    }

    /**
     * Adjusts var-args parameters types length to given length.
     *
     * @param declaredTypes the declared parameter types
     * @param length        the length of arguments
     * @return the adjusted parameters types for match
     */
    private static Class<?>[] adjustVarArgsTypesTo(final Class<?>[] declaredTypes, final int length) {
        final int varargsIndex = declaredTypes.length - 1;
        if (varargsIndex <= length && declaredTypes[varargsIndex].isArray()) {
            // declared types contains varargs, repeat types length to given length
            final Class<?>[] adjustTypes = new Class<?>[length];
            System.arraycopy(declaredTypes, 0, adjustTypes, 0, varargsIndex);
            Arrays.fill(adjustTypes, varargsIndex, length, declaredTypes[varargsIndex].getComponentType());
            return adjustTypes;
        }
        return declaredTypes;
    }

    private static <T extends Member> int matchesCompare(final T memberA, final Class<?>[] typesA,
                                                         final T memberB, final Class<?>[] typesB,
                                                         final Class<?>[] argumentTypes) {
        if (null == typesA) {
            // no best match, set the matched as best match.
            return 1;
        }

        if (isVarArgs(memberA) != isVarArgs(memberB)) {
            // var-args and non-var-args, best match is non-var-args.
            if (isVarArgs(memberB)) {
                return 1;
            } else {
                return -1;
            }
        }

        // already has matched candidate, compare its.
        boolean bIsSubclass = isCompatible(typesA, typesB, true);
        boolean bIsSuperclass = isCompatible(typesB, typesA, true);
        if (bIsSubclass && bIsSuperclass) {
            // the current candidate and best candidate are compatible with each other, using non-synthetic
            bIsSubclass = !memberB.isSynthetic();
            bIsSuperclass = !memberA.isSynthetic();
        }
        if (bIsSubclass == bIsSuperclass) {
            // if compatible with each other, compare without the autoboxing.
            final boolean bestTypesIsExactly = isCompatible(typesA, argumentTypes, false);
            final boolean declaredTypesIsExactly = isCompatible(typesB, argumentTypes, false);
            if (bestTypesIsExactly == declaredTypesIsExactly) {
                return 0;
            } else if (declaredTypesIsExactly) {
                return 1;
            }
        } else if (bIsSubclass) {
            return 1;
        }
        return -1;
    }

    private static boolean isVarArgs(final Member member) {
        if (member instanceof Constructor<?>) {
            return ((Constructor<?>) member).isVarArgs();
        }
        if (member instanceof Method) {
            return ((Method) member).isVarArgs();
        }
        return false;
    }


    /**
     * Whether the declared types is compatible with the value of the given types.
     *
     * @param declaredTypes the declared types
     * @param actualTypes   the actual types
     * @param autoboxing    true if auto(un)boxing is allowed
     * @return true if can apply
     */
    private static boolean isCompatible(final Class<?>[] declaredTypes, final Class<?>[] actualTypes, final boolean autoboxing) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < declaredTypes.length; i++) {
                if (!isCompatible(declaredTypes[i], actualTypes[i], autoboxing)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Whether the declared type is compatible with the value of the given type.
     *
     * @param declaredType the declared type
     * @param actualType   the actual type
     * @param autoboxing   true if auto(un)boxing is allowed
     * @return true if can apply
     */
    private static boolean isCompatible(final Class<?> declaredType, final Class<?> actualType, final boolean autoboxing) {
        final Class<?> finalDeclaredType = autoboxing ? Primitives.wrap(declaredType) : declaredType;
        final Class<?> finalActualType = null != actualType && autoboxing ? Primitives.wrap(actualType) : actualType;
        return null == finalActualType ? !finalDeclaredType.isPrimitive() : finalDeclaredType.isAssignableFrom(finalActualType);
    }

    /* *****************************************
     *                 INVOKER
     * *************************************** */

    /**
     * Invokes the specified constructor with the arguments.
     *
     * @param ctor the constructor to invoke
     * @param args the arguments to invoke
     * @param <T>  the type of invoke
     * @return the instance
     */
    private static <T> T doInvoke(final Constructor<T> ctor, Object... args) {
        try {
            args = ctor.isVarArgs() ? makeVarArgs(ctor.getParameterTypes()) : args;
            return ctor.newInstance(args);
        } catch (Exception ex) {
            return handleReflectionException(ex);
        }
    }

    /**
     * Invokes the specified method with the arguments.
     *
     * @param invoker   the method to invoke
     * @param target    the target to invoke
     * @param arguments the arguments to invoke
     * @param <R>       the result type of invoke
     * @return the result of invoke
     */
    @SuppressWarnings("unchecked")
    private static <R> R doInvoke(final Method invoker, final Object target, final Object... arguments) {
        try {
            final Object[] args = invoker.isVarArgs() ? makeVarArgs(invoker.getParameterTypes(), arguments) : arguments;
            return (R) invoker.invoke(target, args);
        } catch (Exception ex) {
            return handleReflectionException(ex);
        }
    }

    /**
     * Converts the specified arguments to varargs to invoke.
     * <p>
     * declared parameter types     arguments          result
     * int, int, int...             1,2                1,2,[]
     * int, int, int...             1,2,3              1,2,[3]
     * int, int, int...             1,2,3,4,5          1,2,[3,4,5]
     *
     * @param declaredTypes the declared parameter types of var-args method declared
     * @param arguments     the actual arguments to invoke
     * @return the varargs to invoke
     */
    private static Object[] makeVarArgs(final Class<?>[] declaredTypes, Object... arguments) {
        if (1 > declaredTypes.length || !declaredTypes[declaredTypes.length - 1].isArray()) {
            throw new IllegalStateException("declared parameter types not contains varargs type");
        }
        if (arguments.length < declaredTypes.length - 1) {
            // missing arguments (the arguments length < declared parameters length - varargs).
            throw new IllegalArgumentException("wrong number of arguments");
        }

        final Object[] invokeArgs = new Object[declaredTypes.length];
        final int varargsIndex = declaredTypes.length - 1;
        System.arraycopy(arguments, 0, invokeArgs, 0, varargsIndex);

        if (arguments.length == varargsIndex) {
            // missing varargs
            invokeArgs[varargsIndex] = Array.newInstance(declaredTypes[varargsIndex].getComponentType(), 0);
        } else if (arguments.length == declaredTypes.length) {
            // contains varargs and varargs length = 1.
            final Object lastArgs = arguments[arguments.length - 1];
            if (null != lastArgs && lastArgs.getClass().isArray()) {
                // last argument using array apply to varargs.
                invokeArgs[varargsIndex] = lastArgs;
            } else {
                // one argument apply to varargs
                final Object varArgs = Array.newInstance(declaredTypes[varargsIndex].getComponentType(), 1);
                Array.set(varArgs, 0, lastArgs);
                invokeArgs[varargsIndex] = varArgs;
            }
        } else {
            // contains multi-arguments apply to varargs.
            final int mergeToVarargsLen = arguments.length - varargsIndex;
            final Object varargs = Array.newInstance(declaredTypes[declaredTypes.length - 1].getComponentType(), mergeToVarargsLen);
            for (int i = 0; i < mergeToVarargsLen; i++) {
                Array.set(varargs, i, arguments[varargsIndex + i]);
            }
            invokeArgs[varargsIndex] = varargs;
        }
        return invokeArgs;
    }

    /**
     * Handles the reflection exception.
     *
     * @param ex  the reflection exception
     * @param <R> the any type
     * @return not be any return
     */
    private static <R> R handleReflectionException(final Exception ex) {
        if (ex instanceof NoSuchFieldException) {
            throw new IllegalStateException("Field not found: " + ex.getMessage(), ex);
        }
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage(), ex);
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access constructor/field/method: " + ex.getMessage(), ex);
        }
        if (ex instanceof InstantiationException) {
            throw new IllegalStateException("Could not create instance:" + ex.getMessage(), ex);
        }
        if (ex instanceof InvocationTargetException) {
            throw new IllegalStateException(ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Gets the caller of the method, skip system class.
     *
     * @param <T> the class type
     * @return the caller class
     */
    public static <T> Class<T> getGrandCallerClass() {
        return getGrandCallerClass(true);
    }

    /**
     * Gets the caller class of the method.
     *
     * @param skipSystemClass skip system class?
     * @param <T>             the class type
     * @return the caller class
     */
    public static <T> Class<T> getGrandCallerClass(final boolean skipSystemClass) {
        int stackFrameCount = 3;
        Class<T> callerClass = findCallerClass(stackFrameCount);
        while (null != callerClass && skipSystemClass && null == callerClass.getClassLoader()) {
            // looks like a system class
            callerClass = findCallerClass(++stackFrameCount);
        }
        if (null == callerClass) {
            callerClass = findCallerClass(2);
        }
        return callerClass;
    }

    /**
     * Returns the class this method was called 'framesToSkip' frames up the caller hierarchy.
     * <p>
     * NOTE:
     * <b>Extremely expensive!
     * Please consider not using it.
     * These aren't the droids you're looking for!</b>
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> findCallerClass(final int framesToSkip) {
        try {
            final Class<?>[] stack = HolderSecurityManager.INSTANCE.getStack();
            final int indexFromTop = 1 + framesToSkip;
            return (Class<T>) (stack.length > indexFromTop ? stack[indexFromTop] : null);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * TODO DOCME.
     *
     * @return complete me!
     */
    public static StackTraceElement getGrandCallerStackTrace() {
        return getGrandCallerStackTrace(true);
    }

    /**
     * TODO DOCME.
     *
     * @param skipSystemClass if true skip system class
     * @return complete me
     */
    public static StackTraceElement getGrandCallerStackTrace(final boolean skipSystemClass) {
        int stackFrameCount = 3;
        StackTraceElement callerStackTrace = findCallerStackTrace(stackFrameCount);
        while (null != callerStackTrace && skipSystemClass
                // look like system class
                && (callerStackTrace.isNativeMethod() || null == getClassLoader(callerStackTrace.getClassName()))) {
            callerStackTrace = findCallerStackTrace(++stackFrameCount);
        }
        if (null == callerStackTrace) {
            int len = new Throwable().getStackTrace().length;
            callerStackTrace = (new Throwable()).getStackTrace()[len - 1];
        }
        return callerStackTrace;
    }

    /**
     * 获取调用栈信息.
     *
     * @param framesToSkip 跳过的frame层数, 当framesToSkip=0则获取调用该方法的调用者堆栈
     * @return 堆栈信息
     */
    public static StackTraceElement findCallerStackTrace(final int framesToSkip) {
        final StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
        final int indexFromTop = 1 + framesToSkip;
        return stackTrace.length > indexFromTop ? new Throwable().getStackTrace()[indexFromTop] : null;
    }

    /**
     * Gets the class loader for the given class.
     *
     * @param className class full name
     * @return the class loader if the class is exists, otherwise null
     */
    private static ClassLoader getClassLoader(final String className) {
        ClassLoader loader = null;
        try {
            loader = null != className ? Class.forName(className).getClassLoader() : null;
        } catch (final ClassNotFoundException e) {
            // ignore
        }
        return loader;
    }

    /**
     * Call-stack holder.
     */
    private static class HolderSecurityManager extends SecurityManager {
        private static final HolderSecurityManager INSTANCE = new HolderSecurityManager();

        public Class<?>[] getStack() {
            return getClassContext();
        }
    }
}
