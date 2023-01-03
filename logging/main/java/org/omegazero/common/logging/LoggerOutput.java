/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

/**
 * Represents a location where log messages generated by {@link StandardLogger}s are written to.
 *
 * @since 2.10
 */
public interface LoggerOutput {

	/**
	 * Writes the given log line to this {@code LoggerOutput}.
	 *
	 * @param line The log message
	 * @param markup Special terminal escape sequences used for text markup
	 */
	public void writeLine(String line, String markup);

	/**
	 * Flushes any buffered data to the underlying output.
	 */
	public void flush();

	/**
	 * Closes this {@code LoggerOutput}, flushing any remaining data and freeing resources. After a call to this method, the behavior of other methods in this interfaces is undefined.
	 *
	 * @since 2.11.0
	 */
	public default void close(){
	}
}