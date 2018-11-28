/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.thread;

import java.util.concurrent.TimeUnit;

/**
 * Threads utils.
 *
 * @author vacoor
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class Threads {

    /**
     * Non-instantiate.
     */
    private Threads() {
    }

    /**
     * Performs a {@link Thread#sleep(long, int) Thread.sleep}.
     *
     * @param millis the minimum millis to sleep. If less than or equal to zero, do not sleep at all.
     */
    public static void sleep(final long millis) {
        sleep(millis, TimeUnit.MILLISECONDS);
    }


    /**
     * Performs a {@link Thread#sleep(long, int) Thread.sleep} using the time unit.
     *
     * @param timeout the minimum time to sleep. If less than or equal to zero, do not sleep at all.
     * @param unit    the time unit
     */
    public static void sleep(final long timeout, final TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
