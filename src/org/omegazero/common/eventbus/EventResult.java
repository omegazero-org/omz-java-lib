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

import java.util.List;

/**
 * Contains result data of the execution of an {@link Event}, returned by {@link EventBus#dispatchEventRes(Event, Object...)}.
 * 
 * @since 2.1
 */
public class EventResult {

	int listeners;
	Object returnValue;
	List<Object> returnValues;

	EventResult() {
	}


	/**
	 * Returns the number of event handlers that were executed.
	 * 
	 * @return The number of executed event handlers
	 */
	public int getListeners() {
		return this.listeners;
	}

	/**
	 * Returns the single return value of the event execution. If the event was configured to collect {@linkplain Event#isIncludeAllReturns() all return values}, this method
	 * returns <code>null</code>.
	 * 
	 * @return The single return value
	 */
	public Object getReturnValue() {
		return this.returnValue;
	}

	/**
	 * Returns the list of all collected return values, excluding <code>null</code>. If the event was not configured to collect {@linkplain Event#isIncludeAllReturns() all
	 * return values}, this method returns <code>null</code>.
	 * 
	 * @return The list of all return values
	 */
	public List<Object> getReturnValues() {
		return this.returnValues;
	}

	@Override
	public String toString() {
		if(this.returnValue != null)
			return "EventResult[listeners=" + this.listeners + " returnValue=" + this.returnValue + "]";
		else
			return "EventResult[listeners=" + this.listeners + " returnValues=" + this.returnValues.toString() + "]";
	}
}
