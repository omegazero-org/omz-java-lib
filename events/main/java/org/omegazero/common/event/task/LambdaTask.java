/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link Task} based on a {@link Consumer}.
 * 
 * @since 2.6
 */
public class LambdaTask extends AbstractTask {


	private Consumer<Object[]> handler;

	/**
	 * Creates a new {@code LambdaTask}.
	 * 
	 * @param handler The task handler
	 * @param args    The arguments to pass to the task handler when this task is executed
	 * @see AbstractTask#AbstractTask(Object[])
	 */
	public LambdaTask(Consumer<Object[]> handler, Object[] args) {
		super(args);
		this.handler = Objects.requireNonNull(handler);
	}

	/**
	 * Creates a new {@code LambdaTask}.
	 * 
	 * @param handler  The task handler
	 * @param args     The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task
	 * @see AbstractTask#AbstractTask(Object[], int)
	 */
	public LambdaTask(Consumer<Object[]> handler, Object[] args, int priority) {
		super(args, priority);
		this.handler = Objects.requireNonNull(handler);
	}


	/**
	 * Removes the reference to the handler passed in the constructor.
	 *
	 * @since 2.9.2
	 */
	public void destroy(){
		this.handler = null;
	}


	@Override
	protected void execute(Object[] args) throws Exception {
		if(this.handler == null)
			throw new IllegalStateException("This task is destroyed");
		this.handler.accept(args);
	}
}
