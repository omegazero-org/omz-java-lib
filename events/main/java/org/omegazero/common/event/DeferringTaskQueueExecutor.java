/*
 * Copyright (C) 2023 omegazero.org / warp03
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.util.Queue;
import java.util.function.Consumer;

import org.omegazero.common.event.task.Task;

/**
 * An {@link AbstractTaskQueueExecutor} deferring all queued tasks until another {@link AbstractTaskQueueExecutor} implementation is available.
 * An object of this class should be used as a delegate for a {@link DelegatingTaskQueueExecutor}.
 * <p>
 * This class is thread-safe.
 *
 * @since 2.12.0
 */
public class DeferringTaskQueueExecutor extends AbstractTaskQueueExecutor {

	private final int maxDeferredTasks;

	private Queue<Task> deferQueue = new java.util.LinkedList<>();
	private AbstractTaskQueueExecutor delegate = null; // usually not used, only a protection against race conditions
	private Consumer<Throwable> errorHandler = null;

	/**
	 * Creates a new {@link DeferringTaskQueueExecutor} with parameter <i>maxDeferredTasks</i> set to 1024.
	 */
	public DeferringTaskQueueExecutor(){
		this(1024);
	}

	/**
	 * Creates a new {@link DeferringTaskQueueExecutor}.
	 *
	 * @param maxDeferredTasks The maximum number of tasks to defer before rejecting {@link #queue(Task)} calls
	 */
	public DeferringTaskQueueExecutor(int maxDeferredTasks){
		this.maxDeferredTasks = maxDeferredTasks;
	}


	/**
	 * Transfers all deferred tasks of this {@code DeferringTaskQueueExecutor} to the given {@link AbstractTaskQueueExecutor}.
	 * The defer queue is cleared and this {@code DeferringTaskQueueExecutor} can be used further.
	 *
	 * @param destination The {@link AbstractTaskQueueExecutor} to transfer the deferred tasks to
	 */
	public synchronized void transferTasks(AbstractTaskQueueExecutor destination){
		while(!this.deferQueue.isEmpty())
			destination.queue(this.deferQueue.remove());
	}

	/**
	 * Transfers all deferred tasks of this {@code DeferringTaskQueueExecutor} to a new delegate,
	 * then replaces the delegate of a {@link DelegatingTaskQueueExecutor}, where the previous delegate was this object, with this new delegate.
	 * <p>
	 * If an error handler was set for this {@code DeferringTaskQueueExecutor}, it is also set as the error handler for the new delegate.
	 * <p>
	 * After a call to this method, this {@code DeferringTaskQueueExecutor} must no longer be used.
	 *
	 * @param parent The {@link DelegatingTaskQueueExecutor} to set the new delegate for
	 * @param newDelegate The new delegate
	 * @see #transferTasks(AbstractTaskQueueExecutor)
	 */
	public synchronized void replaceFor(DelegatingTaskQueueExecutor parent, AbstractTaskQueueExecutor newDelegate){
		if(this.errorHandler != null)
			newDelegate.setErrorHandler(this.errorHandler);
		this.transferTasks(newDelegate);
		parent.setDelegate(newDelegate);
		this.delegate = newDelegate;
		this.deferQueue = null;
	}


	@Override
	public synchronized boolean queue(Task task){
		if(this.deferQueue != null){
			if(this.deferQueue.size() >= this.maxDeferredTasks)
				throw new IllegalStateException("maxDeferredTasks reached (" + this.maxDeferredTasks + ")");
			return this.deferQueue.add(task);
		}else if(this.delegate != null){
			return this.delegate.queue(task);
		}else
			throw new IllegalStateException("This DeferringTaskQueueExecutor has exited");
	}

	@Override
	public synchronized boolean unqueue(Task task){
		if(this.deferQueue != null){
			return this.deferQueue.remove(task);
		}else if(this.delegate != null){
			return this.delegate.unqueue(task);
		}else
			throw new IllegalStateException("This DeferringTaskQueueExecutor has exited");
	}

	@Override
	public boolean exit(boolean blocking){
		this.deferQueue = null;
		this.delegate = null;
		return false;
	}

	@Override
	public void setErrorHandler(Consumer<Throwable> errorHandler){
		this.errorHandler = errorHandler;
	}
}
