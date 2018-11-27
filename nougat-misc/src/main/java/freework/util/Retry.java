/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.concurrent.TimeUnit;

/**
 * 重试工具类
 *
 * @author vacoor
 */
public abstract class Retry {

    /**
     * 可重试方法
     */
    public interface Command<V> {

        V call() throws Throwable;

    }


    /**
     * 当发生给定类型异常时, 进行重试, 超出重试条件时直接抛出发生的异常
     *
     * @param type       触发重试的异常类型
     * @param maxRetries 最大重试次数
     * @param interval   重试间隔毫秒
     * @param cmd        重试方法
     * @throws Throwable 当不符合重试条件时发生的异常
     */
    public static <V> V retryCount(Class<? extends Throwable> type, int maxRetries, long interval, Command<V> cmd) throws Throwable {
        int tries = 0;
        V ret = null;
        Throwable ex = null;

        while (null == ex || (type.isInstance(ex) && tries++ < maxRetries)) {
            try {
                if (null != ex) {
                    Thread.sleep(interval);
                }
                ex = null;
                ret = cmd.call();
                break;
            } catch (Throwable t) {
                ex = t;
            }
        }

        if (null == ex) {
            return ret;
        }
        throw ex;
    }

    /**
     * 当发生给定类型异常时, 进行重试, 超出重试条件时直接抛出发生的异常
     *
     * @param type      触发重试的异常类型
     * @param timeoutMs 重试超时时间毫秒
     * @param interval  重试间隔毫秒
     * @param cmd       重试方法
     * @throws Throwable 当不符合重试条件时发生的异常
     */
    public static <V> V retryTime(Class<? extends Throwable> type, long timeoutMs, long interval, Command<V> cmd) throws Throwable {
        long timeout = System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeoutMs, TimeUnit.MILLISECONDS);

        V ret = null;
        Throwable ex = null;

        while (null == ex || (type.isInstance(ex) && System.nanoTime() < timeout)) {
            try {
                if (null != ex) {
                    Thread.sleep(interval);
                }
                ex = null;
                ret = cmd.call();
                break;
            } catch (Throwable t) {
                ex = t;
            }
        }

        if (null == ex) {
            return ret;
        }
        throw ex;
    }

    private Retry() {
    }
}
