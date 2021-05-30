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

import java.util.ArrayList;
import java.util.List;

public class EventResult {

	private int listeners;
	private Object returnValue;
	private final List<Object> returnValues = new ArrayList<Object>();


	public int getListeners() {
		return listeners;
	}

	protected void setListeners(int listeners) {
		this.listeners = listeners;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	protected void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public List<Object> getReturnValues() {
		return returnValues;
	}

	@Override
	public String toString() {
		if(this.returnValue != null)
			return "EventResult[listeners=" + this.listeners + " returnValue=" + this.returnValue + "]";
		else
			return "EventResult[listeners=" + this.listeners + " returnValues=" + this.returnValues.toString() + "]";
	}
}
