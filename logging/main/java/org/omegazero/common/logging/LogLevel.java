/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Covered Software is provided under this License on an "as is" basis, without warranty of any kind,
 * either expressed, implied, or statutory, including, without limitation, warranties that the Covered Software
 * is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * The entire risk as to the quality and performance of the Covered Software is with You.
 */
package org.omegazero.common.logging;

/**
 * Represents the importance level of a log message created by a {@link Logger}.
 *
 * @since 2.1
 */
public enum LogLevel {

	/**
	 * The highest log level. Includes very detailed messages about application state or operations.
	 */
	TRACE("trace", 5, "0;90"),
	/**
	 * Log level including messages for basic debugging.
	 */
	DEBUG("debug", 4, "0;37"),
	/**
	 * Information messages, relevant for application users.
	 */
	INFO("info", 3, "0;97"),
	/**
	 * Information messages that may need additional attention.
	 */
	WARN("warn", 2, "0;93"),
	/**
	 * Messages indicating unusual error conditions, possibly requiring user intervention.
	 */
	ERROR("error", 1, "0;91"),
	/**
	 * Messages requiring immediate user intervention, because the application is no longer functioning properly.
	 */
	FATAL("fatal", 0, "41;97");

	private final String label;
	private final int level;
	private final String color;

	private LogLevel(String label, int level, String colorCode) {
		this.label = label;
		this.level = level;
		this.color = "\u001b[" + colorCode + "m";
	}

	/**
	 * Returns an integer representation of a log level, starting with 0 being the lowest log level ({@link #FATAL}).
	 *
	 * @return The log level as an integer
	 */
	public int level(){
		return this.level;
	}

	/**
	 * Returns the ANSI escape sequence string for setting the output terminal text to an appropriate color.
	 *
	 * @return The escape sequence
	 */
	public String color(){
		return this.color;
	}

	@Override
	public String toString(){
		return this.label;
	}
}
