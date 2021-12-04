/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

import java.lang.reflect.Method;

/**
 * A {@link Task} based on reflection.
 * 
 * @since 2.6
 */
public class ReflectTask extends Task {


	private final Method method;
	private final Object callerInstance;

	/**
	 * Creates a new {@link ReflectTask}.
	 * 
	 * @param method         The task handler method
	 * @param callerInstance The instance to call the method with. May be <code>null</code> if the method is static
	 * @param args           The arguments to pass to the task handler when this task is executed
	 * @see Task#Task(Object[])
	 */
	public ReflectTask(Method method, Object callerInstance, Object[] args) {
		super(args);
		this.method = method;
		this.callerInstance = callerInstance;
	}

	/**
	 * Creates a new {@link ReflectTask}.
	 * 
	 * @param method         The task handler method
	 * @param callerInstance The instance to call the method with. May be <code>null</code> if the method is static
	 * @param args           The arguments to pass to the task handler when this task is executed
	 * @param priority       The priority of this task
	 * @see Task#Task(Object[], int)
	 */
	public ReflectTask(Method method, Object callerInstance, Object[] args, int priority) {
		super(args, priority);
		this.method = method;
		this.callerInstance = callerInstance;
	}


	@Override
	public void execute(Object[] args) throws ReflectiveOperationException {
		this.method.invoke(this.callerInstance, args);
	}

	public Method getMethod() {
		return this.method;
	}

	public Object getCallerInstance() {
		return this.callerInstance;
	}
}
