/*
 * Copyright (C) 2023 omegazero.org / warp03
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.omegazero.common.event.task.LambdaTask;
import org.omegazero.common.event.task.ReflectTask;
import org.omegazero.common.event.task.RunnableTask;
import org.omegazero.common.event.task.Task;

/**
 * Used for queuing and running {@link Task}s. The base class for a {@link TaskQueueExecutor}.
 * <p>
 * This class is thread-safe.
 *
 * @since 2.12.0
 */
public abstract class AbstractTaskQueueExecutor {


	/**
	 * Queues a task to be executed by any available worker thread.
	 *
	 * @param task The task to queue
	 * @return <code>true</code> if the task was successfully queued
	 * @see #unqueue(Task)
	 */
	public abstract boolean queue(Task task);

	/**
	 * Removes the given task from the queue.
	 *
	 * @param task The task to remove
	 * @return <code>true</code> if the task was queued previously and removed successfully
	 * @see #queue(Task)
	 * @see java.util.Queue#remove(Object)
	 */
	public abstract boolean unqueue(Task task);

	/**
	 * Shuts this {@code AbstractTaskQueueExecutor} down by gracefully stopping the worker threads.
	 *
	 * @param blocking {@code true} to wait for all worker threads to exit
	 * @return <code>true</code> if the calling thread was interrupted while waiting for the worker threads to exit
	 */
	public abstract boolean exit(boolean blocking);

	/**
	 * Sets the error handler that will be called when an error occurs while executing a task in any of the worker threads.
	 * <p>
	 * If this handler is not set, the error will be {@linkplain Throwable#printStackTrace() printed to <code>stderr</code>}.
	 *
	 * @param errorHandler The error handler, or {@code null} to remove an existing error handler
	 */
	public abstract void setErrorHandler(Consumer<Throwable> errorHandler);


	/**
	 * Creates a new {@link ReflectTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 *
	 * @param method The task handler method
	 * @param callerInstance The instance to call the method with. May be <code>null</code> if the method is static
	 * @param args The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see ReflectTask#ReflectTask(Method, Object, Object[], int)
	 */
	public boolean queue(java.lang.reflect.Method method, Object callerInstance, int priority, Object... args) {
		if(method == null || callerInstance == null)
			throw new NullPointerException();
		return this.queue(new ReflectTask(method, callerInstance, args, priority));
	}

	/**
	 * Creates a new {@link RunnableTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 *
	 * @param handler The task handler
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @since 2.9
	 * @see RunnableTask#RunnableTask(Runnable, int)
	 */
	public boolean queue(Runnable handler, int priority) {
		if(handler == null)
			throw new NullPointerException();
		return this.queue(new RunnableTask(handler, priority));
	}

	/**
	 * Creates a new {@link LambdaTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 *
	 * @param handler The task handler
	 * @param args The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see LambdaTask#LambdaTask(Consumer, Object[], int)
	 */
	public boolean queue(Consumer<Object[]> handler, int priority, Object... args) {
		if(handler == null)
			throw new NullPointerException();
		return this.queue(new LambdaTask(handler, args, priority));
	}

	/**
	 * Shuts this {@code AbstractTaskQueueExecutor} down by gracefully stopping the worker threads.
	 * <p>
	 * Equivalent to a call to:
	 * <pre><code>
	 * {@link #exit(boolean) exit}(false)
	 * </code></pre>
	 */
	public void exit(){
		this.exit(false);
	}
}
