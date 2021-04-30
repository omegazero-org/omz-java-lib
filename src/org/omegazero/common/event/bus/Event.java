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
package org.omegazero.common.event.bus;

import java.util.Objects;

import org.omegazero.common.util.ReflectionUtil;

public class Event {

	private final String name;
	private final String methodName;
	private final boolean cancelable;
	private final Class<?>[] params;

	private final String eventSignature;

	private boolean canceled;

	/**
	 * See {@link Event#Event(String, String, boolean, Class[])}<br>
	 * <br>
	 * The <b>methodName</b> will be set to <b>name</b> with no arguments<br>
	 * The event defaults to being non-cancelable.
	 * 
	 * @param name The unique name of this event. Further meaning is usage-defined
	 */
	public Event(String name) {
		this(name, name, false, new Class<?>[0]);
	}

	/**
	 * See {@link Event#Event(String, String, boolean, Class[])}<br>
	 * <br>
	 * The <b>methodName</b> will be set to <b>name</b><br>
	 * The event defaults to being non-cancelable.
	 * 
	 * @param name   The unique name of this event. Further meaning is usage-defined
	 * @param params The parameter types of the called method for this event. May be of length 0.
	 */
	public Event(String name, Class<?>[] params) {
		this(name, name, false, params);
	}

	/**
	 * See {@link Event#Event(String, String, boolean, Class[])}<br>
	 * <br>
	 * The event defaults to being non-cancelable.
	 * 
	 * @param name       The unique name of this event. Further meaning is usage-defined
	 * @param methodName The name of the method that should be called when this event occurs
	 * @param params     The parameter types of the called method for this event. May be of length 0.
	 */
	public Event(String name, String methodName, Class<?>[] params) {
		this(name, methodName, false, params);
	}

	/**
	 * Creates a general-purpose event object.
	 * 
	 * @param name       The unique name of this event. Further meaning is usage-defined
	 * @param methodName The name of the method that should be called when this event occurs
	 * @param cancelable Whether this event may be canceled using {@link Event#cancel()}
	 * @param params     The parameter types of the called method for this event. May be of length 0.
	 */
	public Event(String name, String methodName, boolean cancelable, Class<?>[] params) {
		this.name = Objects.requireNonNull(name);
		this.methodName = Objects.requireNonNull(methodName);
		this.cancelable = cancelable;
		this.params = Objects.requireNonNull(params);
		this.eventSignature = Event.createEventSignature(methodName, params);
	}


	/**
	 * Returns the unique name of this event.
	 * 
	 * @return The unique name of this event
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the method name of this event, which is the name of the method that is called when this event occurs.<br>
	 * <br>
	 * Further details are usage-defined.
	 * 
	 * @return The method name of this event
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return <b>true</b> if this event may be canceled using {@link Event#cancel()}.
	 */
	public boolean isCancelable() {
		return cancelable;
	}

	public Class<?>[] getParams() {
		return params;
	}

	/**
	 * 
	 * @return A string containing the method name and a string representation of method parameters
	 */
	public String getEventSignature() {
		return eventSignature;
	}


	/**
	 * Returns <b>true</b> if this event was canceled.
	 * 
	 * @return The method name of this event
	 */
	public boolean isCanceled() {
		return this.cancelable && canceled;
	}

	public void setCanceled(boolean canceled) {
		if(canceled && !this.cancelable)
			throw new UnsupportedOperationException("Event '" + this.name + "' is not cancelable");
		this.canceled = canceled;
	}

	/**
	 * Cancels this event. An event may only be canceled if it is explicitly cancelable ({@link Event#isCancelable()} returns <b>true</b>), otherwise, an
	 * {@link UnsupportedOperationException} is thrown.<br>
	 * <br>
	 * Behavior when an event is canceled is usage-defined.
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
	 * 
	 * @return <b>true</b> if this event has a variable state; otherwise, this event object may be safely used multiple times and/or concurrently
	 */
	public boolean isStateful() {
		// cancelable is the only variable thing here, everything else is final and cannot change
		return this.cancelable;
	}


	public static Class<?>[] getTypesFromObjectArray(Object[] obj) {
		Class<?>[] types = new Class<?>[obj.length];
		for(int i = 0; i < obj.length; i++){
			if(obj[i] == null)
				types[i] = void.class;
			else
				types[i] = obj[i].getClass();
		}
		return types;
	}

	public static String createEventSignature(String methodName, Class<?>[] params) {
		return ReflectionUtil.getMethodSignature(void.class, params) + " " + methodName;
	}
}
