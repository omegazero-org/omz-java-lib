/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * Represents a task including arguments passed to a task handler and a priority.
 * 
 * @since 2.6
 */
public abstract class Task implements Runnable, Comparable<Task> {


	private final Object[] args;
	private final int priority;

	/**
	 * Creates a new {@link Task} with priority <code>0</code>.
	 * 
	 * @param args The arguments to pass to the task handler when this task is executed
	 */
	public Task(Object[] args) {
		this(args, 0);
	}

	/**
	 * Creates a new {@link Task}.
	 * 
	 * @param args     The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. Tasks with a lower priority number will be executed first
	 */
	public Task(Object[] args, int priority) {
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
	 * Compares this {@link Task} to the given <code>Task</code>.<br>
	 * <br>
	 * The return value is negative if this priority task has higher priority than the given priority task (meaning a lower priority number), positive if this task has lower
	 * priority than the given task, and <code>0</code> if the priority of both tasks is equal.
	 * 
	 * @return A negative integer, zero, or a positive integer if this task has higher, equal, or lower priority than the given task, respectively
	 */
	@Override
	public int compareTo(Task o) {
		return this.priority - o.priority;
	}


	/**
	 * Runs this {@link Task}. Exact behavior is implementation-defined.<br>
	 * <br>
	 * General behavior includes running a task handler (for example a method) with the arguments passed in the constructor.
	 * 
	 * @throws ExecutionFailedException If an error occurs during execution of the task
	 */
	@Override
	public final void run() {
		try{
			this.execute(this.args);
		}catch(Exception e){
			throw new ExecutionFailedException(e);
		}
	}


	/**
	 * 
	 * @return The priority of this task
	 */
	public int getPriority() {
		return this.priority;
	}
}
