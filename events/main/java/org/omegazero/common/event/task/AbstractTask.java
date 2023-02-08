/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * A {@link Task} with arguments passed to a task handler.
 * 
 * @since 2.9
 * @apiNote Before version 2.9, this class was named {@code Task}.
 */
public abstract class AbstractTask implements Task {


	private final Object[] args;
	private final int priority;

	/**
	 * Creates a new {@code AbstractTask} with priority <code>0</code>.
	 * 
	 * @param args The arguments to pass to the task handler when this task is executed
	 */
	public AbstractTask(Object[] args) {
		this(args, 0);
	}

	/**
	 * Creates a new {@code AbstractTask}.
	 * 
	 * @param args The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. Tasks with a lower priority number will be executed first
	 */
	public AbstractTask(Object[] args, int priority) {
		this.args = args;
		this.priority = priority;
	}


	/**
	 * Executes this task with the given arguments.
	 * 
	 * @param args The arguments to pass to the task handler
	 * @throws Exception If an error occurs
	 */
	protected abstract void execute(Object[] args) throws Exception;


	/**
	 * Runs this {@code AbstractTask}. Exact behavior is implementation-defined.
	 * <p>
	 * General behavior includes running a task handler (for example a method) with the arguments passed in the constructor.
	 * 
	 * @throws ExecutionFailedException If an error occurs during execution of the task. {@code ExecutionFailedException}s thrown by the task handler will be propagated unchanged
	 */
	@Override
	public void run() {
		try{
			this.execute(this.args);
		}catch(Exception e){
			if(e instanceof ExecutionFailedException)
				throw (ExecutionFailedException) e;
			else
				throw new ExecutionFailedException(e);
		}
	}


	@Override
	public int getPriority() {
		return this.priority;
	}
}
