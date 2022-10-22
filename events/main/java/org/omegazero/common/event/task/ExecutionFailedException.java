/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * Exception thrown when a {@link Task} or other runnable failed to execute due to an exception.
 * 
 * @since 2.6
 */
public class ExecutionFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	/**
	 * Creates a new {@link ExecutionFailedException} with the specified message.
	 *
	 * @param msg The error message
	 * @since 2.10
	 */
	public ExecutionFailedException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link ExecutionFailedException} with the specified cause.
	 *
	 * @param cause The {@link Throwable} that caused this task to fail
	 */
	public ExecutionFailedException(Throwable cause) {
		this("Error executing task", cause);
	}

	/**
	 * Creates a new {@link ExecutionFailedException} with the specified message and cause.
	 *
	 * @param msg The error message
	 * @param cause The {@link Throwable} that caused this task to fail
	 * @since 2.10
	 */
	public ExecutionFailedException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
