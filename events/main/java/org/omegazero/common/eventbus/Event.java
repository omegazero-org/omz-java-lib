/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Covered Software is provided under this License on an "as is" basis, without warranty of any kind,
 * either expressed, implied, or statutory, including, without limitation, warranties that the Covered Software
 * is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * The entire risk as to the quality and performance of the Covered Software is with You.
 */
package org.omegazero.common.eventbus;

import java.util.Objects;

import org.omegazero.common.util.ReflectionUtil;

/**
 * Primarily used for calling event bus subscribers using an {@link EventBus}. An object of this class contains the method signature of the target method to call when
 * dispatching this <code>Event</code> and several additional parameters for event execution.
 * 
 * @since 2.1
 */
public class Event {

	private final String methodName;
	private final boolean cancelable;
	private final Class<?>[] params;
	private final Class<?> returnType;
	private final boolean includeAllReturns;

	private final String eventSignature;

	private boolean canceled;


	/**
	 * See {@link Event#Event(String, boolean, Class[], Class, boolean)}
	 * <p>
	 * The event defaults to being non-cancelable and configured to not collect all return values from subscribers (<b>includeAllReturns</b> is <code>false</code>). The
	 * default return type is <code>void</code>.
	 * 
	 * @param methodName The name of the method that should be called when this event occurs
	 * @param params     The parameter types of the called method for this event. May be of length 0
	 */
	public Event(String methodName, Class<?>[] params) {
		this(methodName, false, params, void.class, false);
	}

	/**
	 * See {@link Event#Event(String, boolean, Class[], Class, boolean)}
	 * <p>
	 * The event defaults to being non-cancelable and configured to not collect all return values from subscribers (<b>includeAllReturns</b> is <code>false</code>).
	 * 
	 * @param methodName The name of the method that should be called when this event occurs
	 * @param params     The parameter types of the called method for this event. May be of length 0
	 * @param returnType The return type of event listener methods
	 */
	public Event(String methodName, Class<?>[] params, Class<?> returnType) {
		this(methodName, false, params, returnType, false);
	}

	/**
	 * Creates a general-purpose {@link Event} object.
	 * 
	 * @param methodName        The name of the method that should be called when this event occurs
	 * @param cancelable        Whether this event may be canceled using {@link Event#cancel()}
	 * @param params            The parameter types of the called method for this event. May be of length 0
	 * @param returnType        The return type of event listener methods
	 * @param includeAllReturns Include return values from all listener methods instead of just the first
	 */
	public Event(String methodName, boolean cancelable, Class<?>[] params, Class<?> returnType, boolean includeAllReturns) {
		this.methodName = Objects.requireNonNull(methodName);
		this.cancelable = cancelable;
		this.params = Objects.requireNonNull(params);
		this.returnType = Objects.requireNonNull(returnType);
		this.includeAllReturns = includeAllReturns;

		this.eventSignature = Event.createEventSignature(methodName, params, returnType);
	}


	/**
	 * Returns the method name of this event, which is the name of the method that is called when this event occurs.
	 * <p>
	 * Further details are usage-defined.
	 * 
	 * @return The method name of this event
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Whether this event was configured to allow cancellation using {@link Event#cancel()}.
	 * 
	 * @return <b>true</b> if this event may be canceled
	 */
	public boolean isCancelable() {
		return this.cancelable;
	}

	/**
	 * Returns the method parameters of the target method of this event.
	 * 
	 * @return The method parameters of this event
	 */
	public Class<?>[] getParams() {
		return this.params;
	}

	/**
	 * Returns the return type of the target method of this event.
	 * 
	 * @return The return type of this event
	 */
	public Class<?> getReturnType() {
		return this.returnType;
	}

	/**
	 * Whether all event bus subscriber handlers should be called to collect a list of return values when dispatching this event. If this is <code>false</code>, event
	 * execution will be stopped when the first handler returns a non-<code>null</code> value.
	 * 
	 * @return <code>true</code> if all return values should be collected
	 */
	public boolean isIncludeAllReturns() {
		return this.includeAllReturns;
	}

	/**
	 * Returns a string containing the method name and a string representation of method parameters and the return type of the target event method.
	 * 
	 * @return The method signature
	 */
	public String getEventSignature() {
		return this.eventSignature;
	}


	/**
	 * Returns <code>true</code> if this event was canceled.
	 * 
	 * @return <code>true</code> if this event was canceled
	 */
	public boolean isCanceled() {
		return this.cancelable && this.canceled;
	}

	/**
	 * Sets whether this event is canceled. When an event handler calls this method with a value of <code>true</code>, subsequent event handlers will not be called.
	 * 
	 * @param canceled Whether this event is canceled
	 * @throws UnsupportedOperationException If this method is called with a value of <code>true</code> but this event is not {@linkplain #isCancelable() cancelable}
	 */
	public void setCanceled(boolean canceled) {
		if(canceled && !this.cancelable)
			throw new UnsupportedOperationException("Event '" + this.methodName + "' is not cancelable");
		this.canceled = canceled;
	}

	/**
	 * Cancels this event. An event may only be canceled if it is explicitly {@linkplain #isCancelable() cancelable}.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #setCanceled(boolean) setCanceled}(true)</code>
	 * </pre>
	 * 
	 * @throws UnsupportedOperationException If this event is not {@linkplain #isCancelable() cancelable}
	 */
	public void cancel() {
		this.setCanceled(true);
	}


	/**
	 * Resets this event instance to initial values for reuse.
	 */
	public void reset() {
		this.canceled = false;
	}

	/**
	 * Returns <b>true</b> if this event has a variable state. If it does not, this event object may be safely used multiple times and/or concurrently. Otherwise, it should
	 * not be used concurrently and {@link #reset()} must be called before reuse.
	 * 
	 * @return <b>true</b> if this event is stateful
	 */
	public boolean isStateful() {
		// cancelable is the only variable thing here, everything else is final and cannot change
		return this.cancelable;
	}


	/**
	 * Creates a string containing unambiguous information about the given {@code params}, {@code returnType} and name of a method.
	 *
	 * @param methodName A method name
	 * @param params The parameters of the method
	 * @param returnType The return type
	 * @return The string
	 * @see ReflectionUtil#getMethodSignature(Class, Class[])
	 */
	public static String createEventSignature(String methodName, Class<?>[] params, Class<?> returnType) {
		return methodName + ReflectionUtil.getMethodSignature(returnType, params);
	}
}
