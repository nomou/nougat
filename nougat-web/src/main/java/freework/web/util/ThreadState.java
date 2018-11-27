/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.web.util;

/**
 * A {@code ThreadState} instance manages any state that might need to be bound and/or restored during a thread's
 * execution.
 * <h3>Usage</h3>
 * Calling {@link #bind bind()} will place state on the currently executing thread to be accessed later during
 * the thread's execution.
 * <h4>WARNING</h4>
 * After the thread is finished executing, or if an exception occurs, any previous state <b>MUST</b> be
 * {@link #restore restored} to guarantee all threads stay clean in any thread-pooled environment.  This should always
 * be done in a {@code try/finally} block:
 * <pre>
 * ThreadState state = //acquire or instantiate as necessary
 * try {
 *     state.bind();
 *     doSomething(); //execute any logic downstream logic that might need to access the state
 * } <b>finally {
 *     state.restore();
 * }</b>
 * </pre>
 *
 * @since 1.0
 */
public interface ThreadState {

    /**
     * Binds any state that should be made accessible during a thread's execution.  This should typically always
     * be called in a {@code try/finally} block paired with the {@link #restore} call to guarantee that the thread
     * is cleanly restored back to its original state.  For example:
     * <pre>
     * ThreadState state = //acquire or instantiate as necessary
     * <b>try {
     *     state.bind();
     *     doSomething(); //execute any logic downstream logic that might need to access the state
     * } </b> finally {
     *     state.restore();
     * }
     * </pre>
     */
    void bind();

    /**
     * Restores a thread to its state before bind {@link #bind bind} was invoked.  This should typically always be
     * called in a {@code finally} block to guarantee that the thread is cleanly restored back to its original state
     * before {@link #bind bind}'s bind was called.  For example:
     * <pre>
     * ThreadState state = //acquire or instantiate as necessary
     * try {
     *     state.bind();
     *     doSomething(); //execute any logic downstream logic that might need to access the state
     * } <b>finally {
     *     state.restore();
     * }</b>
     * </pre>
     */
    void restore();

    /**
     * Completely clears/removes the {@code ThreadContext} state.  Typically this method should
     * only be called in special cases - it is more 'correct' to {@link #restore restore} a thread to its previous
     * state than to clear it entirely.
     */
    void clear();

}
