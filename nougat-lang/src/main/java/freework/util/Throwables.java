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
@SuppressWarnings({"unused"})
public abstract class Throwables {

    /**
     * Non-instantiate.
     */
    private Throwables() {
    }

    /**
     * 检查异常是否由给定异常引发
     */
    public static boolean causedBy(final Throwable t, final Class<? extends Throwable> causeType) {
        final Set<Throwable> causes = new HashSet<Throwable>();

        Throwable cause = t;
        for (; null != cause && !causeType.isInstance(cause) && !causes.contains(cause); cause = cause.getCause()) {
            causes.add(cause);
        }

        return null != cause && causeType.isInstance(cause);
    }

    public static Throwable getCause(final Throwable t) {
        final Set<Throwable> causes = new HashSet<Throwable>();

        Throwable cause = t;
        while (null != cause.getCause() && !causes.contains(cause)) {
            cause = cause.getCause();
        }
        return cause;
    }

    public static <R> R unchecked(final Throwable cause) {
        return unchecked(null, cause);
    }

    public static <R> R unchecked(final String message, final Throwable cause) {
        return rethrow(cause, message, IllegalStateException.class);
    }

    public static <R, E extends Throwable> R rethrow(final Throwable cause, final Class<E> wrapType) throws E {
        return rethrow(cause, cause.getMessage(), wrapType);
    }

    public static <R, E extends Throwable> R rethrow(final Throwable cause, final String message, final Class<E> wrapType) throws E {
        if (cause instanceof Error) {
            throw (Error) cause;
        }

        final String finalMessage = null != message ? message : cause.getMessage();
        throw Reflect.wrap(wrapType).instantiate(message, cause).<E>get();
    }

    /**
     * 获取异常的堆栈追踪字符串
     *
     * @param t 异常
     * @return 异常堆栈信息
     */
    public static String getStackTraceAsString(Throwable t) {
        final StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }
}
