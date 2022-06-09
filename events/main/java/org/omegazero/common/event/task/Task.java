/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * Represents a {@code Runnable} with a priority.
 * 
 * @since 2.6
 * @apiNote Before version 2.9, this was an abstract class. The class was partially moved to {@link AbstractTask}.
 */
public interface Task extends Runnable, Comparable<Task> {


	/**
	 * Runs this {@link Task}. Exact behavior is implementation-defined.
	 * <p>
	 * General behavior includes running a task handler (for example a method).
	 * 
	 * @throws ExecutionFailedException If an error occurs during execution of the task
	 */
	@Override
	public void run();

	/**
	 * Returns the priority of this task. A lower priority number means this task has a higher priority.
	 * 
	 * @return The priority of this task
	 */
	public int getPriority();

	/**
	 * Compares this {@link Task} to the given <code>Task</code>.
	 * <p>
	 * The return value is negative if this priority task has higher priority than the given priority task (meaning a lower priority number), positive if this task has lower
	 * priority than the given task, and <code>0</code> if the priority of both tasks is equal.
	 * 
	 * @return A negative integer, zero, or a positive integer if this task has higher, equal, or lower priority than the given task, respectively
	 */
	@Override
	public default int compareTo(Task o) {
		return this.getPriority() - o.getPriority();
	}
}
