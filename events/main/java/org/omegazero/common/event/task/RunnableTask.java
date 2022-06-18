/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * A {@link Task} based on a {@link Runnable}.
 * 
 * @since 2.9
 */
public class RunnableTask extends AbstractTask {


	private final Runnable handler;

	/**
	 * Creates a new {@code RunnableTask}.
	 * 
	 * @param handler The task handler
	 */
	public RunnableTask(Runnable handler) {
		super(null);
		this.handler = handler;
	}

	/**
	 * Creates a new {@code RunnableTask}.
	 * 
	 * @param handler The task handler
	 * @param priority The priority of this task
	 */
	public RunnableTask(Runnable handler, int priority) {
		super(null, priority);
		this.handler = handler;
	}


	@Override
	protected void execute(Object[] args) throws Exception {
		this.handler.run();
	}
}
