/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

/**
 * A class used for outputting application messages.
 * <p>
 * Messages may be written line by line with any log level defined in {@link LogLevel}. More verbose messages should generally be written with a higher log level than more important messages.
 * For example, the "debug" log level should be used for log messages useful for basic debugging by an application developer or user with advanced knowledge of the application, while "error"
 * should be used for any messages that may require intervention by any user.
 * <p>
 * TODO: log outputs
 *
 * @since 2.1
 */
public interface Logger {


	/**
	 * Constructs a log message from the given objects and outputs it at the given log level.
	 *
	 * @param level The log level to output this message at
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public void log(LogLevel level, Object... obj);


	/**
	 * Returns the label used to identify this {@code Logger} in log messages.
	 *
	 * @return The label
	 * @since 2.5
	 */
	public String getLabel();


	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#TRACE}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void trace(Object... obj){
		this.log(LogLevel.TRACE, obj);
	}

	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#DEBUG}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void debug(Object... obj){
		this.log(LogLevel.DEBUG, obj);
	}

	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#INFO}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void info(Object... obj){
		this.log(LogLevel.INFO, obj);
	}

	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#WARN}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void warn(Object... obj){
		this.log(LogLevel.WARN, obj);
	}

	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#ERROR}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void error(Object... obj){
		this.log(LogLevel.ERROR, obj);
	}

	/**
	 * {@linkplain #log(LogLevel, Object...) Logs} the given message at {@link LogLevel#FATAL}.
	 *
	 * @param obj The objects to construct a log message from, usually strings
	 */
	public default void fatal(Object... obj){
		this.log(LogLevel.FATAL, obj);
	}


	/**
	 * Utility function to check if debugging is enabled.
	 * 
	 * @return <code>true</code> if the current log level is {@link LogLevel#DEBUG} or higher
	 * @since 2.3
	 */
	public default boolean debug() {
		return LoggerUtil.getLogLevel().level() >= LogLevel.DEBUG.level();
	}

	/**
	 * Checks if this <code>Logger</code> prints log messages, taking into account the configured log level and other logger settings.
	 * 
	 * @param level The log level to check
	 * @return <code>true</code> if this logger prints log messages on the given log level
	 * @since 2.5
	 * @see LoggerUtil#getLogLevel()
	 */
	public default boolean isLogging(LogLevel level) {
		return LoggerUtil.getLogLevel().level() >= level.level();
	}


	/**
	 * Creates a new logger bound to the calling class.
	 * <p>
	 * This function is equivalent to {@link LoggerUtil#createLogger()}.
	 * 
	 * @return The new logger instance
	 * @since 2.3
	 */
	public static Logger create() {
		return LoggerUtil.createLogger(0);
	}
}
