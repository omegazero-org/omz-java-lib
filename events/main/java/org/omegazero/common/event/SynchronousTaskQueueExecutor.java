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
 * An {@link AbstractTaskQueueExecutor} running all tasks passed to {@code queue} methods synchronously (in the same thread).
 * <p>
 * This class is thread-safe. It has little real-world value and should only be used for testing purposes.
 *
 * @since 2.12.0
 */
public class SynchronousTaskQueueExecutor extends AbstractTaskQueueExecutor {

	private Consumer<Throwable> errorHandler = null;

	/**
	 * Creates a new {@link SynchronousTaskQueueExecutor}.
	 */
	public SynchronousTaskQueueExecutor(){
	}


	@Override
	public synchronized boolean queue(Task task){
		try{
			task.run();
		}catch(Exception e){
			if(this.errorHandler != null)
				this.errorHandler.accept(e);
			else
				e.printStackTrace();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A no-op that always returns {@code false}, since tasks are not queued but executed synchronously.
	 */
	@Override
	public synchronized boolean unqueue(Task task){
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A no-op that always returns {@code false}.
	 */
	@Override
	public boolean exit(boolean blocking){
		return false;
	}

	@Override
	public void setErrorHandler(Consumer<Throwable> errorHandler){
		this.errorHandler = errorHandler;
	}
}
