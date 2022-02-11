/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.task;

/**
 * Exception thrown when a {@link Task} fails to execute due to an exception.
 * 
 * @since 2.6
 */
public class ExecutionFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	/**
	 * Creates a new {@link ExecutionFailedException} with the specified cause.
	 * 
	 * @param cause The {@link Throwable} that caused this task to fail
	 */
	public ExecutionFailedException(Throwable cause) {
		super("Error executing task", cause);
	}
}
