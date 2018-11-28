/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import freework.reflect.Reflect;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Throwable utils.
 *
 * @author vacoor
 * @since 1.0
 */
@SuppressWarnings({"PMD.AbstractClassShouldStartWithAbstractNamingRule"})
public abstract class Throwables {

    /**
     * Non-instantiate.
     */
    private Throwables() {
    }

    /**
     * Re-throws the specified throwable as unchecked exception (IllegalStateException).
     *
     * @param cause the throwable
     * @param <R>   the type of return
     * @return never return
     */
    public static <R> R unchecked(final Throwable cause) throws IllegalStateException {
        return unchecked(cause.getMessage(), cause);
    }

    /**
     * Re-throws the specified throwable as unchecked exception (IllegalStateException).
     *
     * @param message the message of unchecked exception
     * @param cause   the throwable
     * @param <R>     the type of return
     * @return never return
     */
    public static <R> R unchecked(final String message, final Throwable cause) throws IllegalStateException {
        return rethrow(IllegalStateException.class, message, cause);
    }

    /**
     * Re-throws the specified throwable as wrap type throwable.
     *
     * @param <R>      the type of return
     * @param <E>      the wrap type
     * @param wrapType the wrap type class of cause
     * @param cause    the cause throwable
     * @return never return
     * @throws E the wrap throwable
     */
    public static <R, E extends Throwable> R rethrow(final Class<E> wrapType, final Throwable cause) throws E {
        return rethrow(wrapType, cause.getMessage(), cause);
    }

    /**
     * Re-throws the specified throwable as wrap type throwable.
     *
     * @param <R>      the type of return
     * @param <E>      the wrap type
     * @param wrapType the wrap type class of cause
     * @param message  the message of wrap throwable
     * @param cause    the cause throwable
     * @return never return
     * @throws E the wrap throwable
     */
    public static <R, E extends Throwable> R rethrow(final Class<E> wrapType, final String message, final Throwable cause) throws E {
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        throw Reflect.wrap(wrapType).instantiate(message, cause).<E>get();
    }

    /**
     * Returns whether the throwable 't' is caused by a given cause type.
     *
     * @param t         the throwable
     * @param causeType the cause type
     * @return true if caused by cause type, otherwise false
     */
    public static boolean causedBy(final Throwable t, final Class<? extends Throwable> causeType) {
        final Set<Throwable> causes = new HashSet<Throwable>();
        Throwable cause = t;
        for (; null != cause && !causeType.isInstance(cause) && !causes.contains(cause); cause = cause.getCause()) {
            causes.add(cause);
        }
        return causeType.isInstance(cause);
    }

    /**
     * Returns the innermost cause of the throwable.
     *
     * @param throwable the throwable
     * @return the innermost cause
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;
        }
        return throwable;
    }

    /**
     * Returns a string containing the result of Throwable#toString(), followed by the full, recursive stack trace of  throwable.
     *
     * @param t the throwable
     * @return the stack trace string
     */
    public static String getStackTraceAsString(final Throwable t) {
        final StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
