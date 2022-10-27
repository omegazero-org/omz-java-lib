/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.config;

/**
 * Exception thrown when an error occurs while loading or processing a configuration.
 * 
 * @since 2.10
 */
public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	/**
	 * Creates a new {@code ConfigurationException} with the specified message.
	 *
	 * @param msg The error message
	 */
	public ConfigurationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@code ConfigurationException} with the specified message and cause.
	 *
	 * @param msg The error message
	 * @param cause The {@code Throwable} cause
	 */
	public ConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
