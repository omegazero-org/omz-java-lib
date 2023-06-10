/*
 * Copyright (C) 2023 omegazero.org / warp03
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.util.function.Consumer;

import org.omegazero.common.event.task.Task;

/**
 * An {@link AbstractTaskQueueExecutor} delegating all tasks to another {@link AbstractTaskQueueExecutor} implementation.
 * <p>
 * This class is thread-safe.
 *
 * @since 2.12.0
 */
public class DelegatingTaskQueueExecutor extends AbstractTaskQueueExecutor {

	private AbstractTaskQueueExecutor delegate;
	private boolean locked = false;

	/**
	 * Creates a new {@link DelegatingTaskQueueExecutor}.
	 *
	 * @param delegate The {@link AbstractTaskQueueExecutor} to delegate tasks to
	 */
	public DelegatingTaskQueueExecutor(AbstractTaskQueueExecutor delegate){
		this.delegate = delegate;
	}


	/**
	 * Sets a new {@link AbstractTaskQueueExecutor} to delegate tasks to.
	 *
	 * @param delegate The new {@link AbstractTaskQueueExecutor}
	 * @throws IllegalStateException If this {@code DelegatingTaskQueueExecutor} was locked using {@link #lock()}
	 */
	public synchronized void setDelegate(AbstractTaskQueueExecutor delegate){
		if(this.locked)
			throw new IllegalStateException("This DelegatingTaskQueueExecutor is locked");
		this.delegate = delegate;
	}

	/**
	 * Locks this {@code DelegatingTaskQueueExecutor}. If locked, the delegate cannot be changed using {@link #setDelegate(AbstractTaskQueueExecutor)} anymore.
	 *
	 * @throws IllegalStateException If this {@code DelegatingTaskQueueExecutor} was locked already
	 */
	public synchronized void lock(){
		if(this.locked)
			throw new IllegalStateException("This DelegatingTaskQueueExecutor was already locked");
		this.locked = true;
	}


	@Override
	public boolean queue(Task task){
		return this.delegate.queue(task);
	}

	@Override
	public boolean unqueue(Task task){
		return this.delegate.unqueue(task);
	}

	@Override
	public boolean exit(boolean blocking){
		return this.delegate.exit(blocking);
	}

	@Override
	public void setErrorHandler(Consumer<Throwable> errorHandler){
		this.delegate.setErrorHandler(errorHandler);
	}
}
