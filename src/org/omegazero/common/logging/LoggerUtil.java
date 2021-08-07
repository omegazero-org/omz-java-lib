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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.omegazero.common.event.Tasks;
import org.omegazero.common.util.PropertyUtil;

public final class LoggerUtil {

	private static final Logger logger = LoggerUtil.createLogger();

	public static final int SAVE_INTERVAL = PropertyUtil.getInt("org.omegazero.common.logging.saveInterval", 300) * 1000;
	public static final int LOG_BUFFER_MAX = PropertyUtil.getInt("org.omegazero.common.logging.logBufferSize", 1024);

	private static final RuntimePermission SET_LOGGER_UTIL_SETTINGS_PERMISSION = new RuntimePermission("setLoggerUtilSettings");
	private static final RuntimePermission SET_LOGGER_OUT_PERMISSION = new RuntimePermission("setLoggerOut");
	private static final RuntimePermission LOG_LISTENER_PERMISSION = new RuntimePermission("logListener");

	private static String logFile = null;
	private static LogLevel logLevel = LogLevel.INFO;

	private static boolean syncFlush = false;
	private static List<String> logBuffer = new ArrayList<>(LOG_BUFFER_MAX);

	private static final List<BiConsumer<LogLevel, String>> listeners = new ArrayList<>();

	public static final PrintStream sysOut = System.out;
	public static final PrintStream sysErr = System.err;
	private static PrintStream loggerOut = sysOut;

	private LoggerUtil() {
	}


	/**
	 * Initializes the logger, setting the maximum log level to <b>level</b> and the log file name, where log messages will be saved to.<br>
	 * <br>
	 * To reduce disk writes and increase logging speed, by default, log messages will only be saved every 5 minutes, if the logger buffer is full or when
	 * {@link LoggerUtil#close()} is called. This may be disabled using {@link LoggerUtil#setSyncFlush(boolean)} by setting it to <b>true</b>.<br>
	 * The log buffer may be flushed explicitly using {@link LoggerUtil#flushLogBuffer()}. The save interval may be changed using the
	 * <code>org.omegazero.common.logging.saveInterval</code> system property by setting it to a number representing the time in seconds before this class is used.
	 * 
	 * @param level The maximum log level for log messages. Log messages higher than this will be omitted. May be <b>null</b>, in which case it will be set to
	 *              {@link LogLevel#INFO}
	 * @param file  The file name to save log messages to. May be <b>null</b> to disable disk saving
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static void init(LogLevel level, String file) {
		checkLoggerSettingsPermission();

		if(level != null)
			LoggerUtil.logLevel = level;
		else
			LoggerUtil.logLevel = LogLevel.INFO;

		LoggerUtil.logFile = file;
		if(file != null){
			Tasks.interval((args) -> {
				LoggerUtil.flushLogBuffer();
			}, SAVE_INTERVAL).daemon();
		}
	}

	/**
	 * Saves the log buffer and resets the LoggerUtil.
	 * 
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static void close() {
		checkLoggerSettingsPermission();
		if(LoggerUtil.logFile != null){
			logger.info("Saving log to '" + logFile + "'");
			LoggerUtil.flushLogBuffer();
		}
		LoggerUtil.logFile = null;
	}


	/**
	 * Creates a new logger bound to the calling class.
	 * 
	 * @return The new logger instance
	 */
	public static Logger createLogger() {
		return createLogger(0);
	}

	protected static Logger createLogger(int off) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[3 + off];
		String[] str = ste.getClassName().split("\\.");
		return new Logger(str[str.length - 1]);
	}

	protected static Logger createLogger(Class<?> cl) {
		return new Logger(cl.getSimpleName());
	}


	protected static void checkLoggerSettingsPermission() {
		checkPermission(SET_LOGGER_UTIL_SETTINGS_PERMISSION);
	}

	protected static void checkLoggerIOPermission() {
		checkPermission(SET_LOGGER_OUT_PERMISSION);
	}

	protected static void checkPermission(java.security.Permission perm) {
		SecurityManager sm = System.getSecurityManager();
		if(sm != null)
			sm.checkPermission(perm);
	}

	/**
	 * Redirects the <code>System.out</code> and <code>System.err</code> to Logger streams, causing messages printed using <code>System.out.[...]</code> or
	 * <code>System.err.[...]</code> to be formatted to standard logger format and printed with log level {@link LogLevel#INFO}.
	 * 
	 * @throws SecurityException If a security manager is present and does not allow reassignment of the standard or logger output streams
	 */
	public static void redirectStandardOutputStreams() {
		checkLoggerIOPermission();
		System.setOut(new PrintStream(new LoggerOutputStream("stdout")));
		System.setErr(new PrintStream(new LoggerOutputStream("stderr")));
	}

	/**
	 * If set to <b>true</b>, all log messages created using a {@link Logger} will be printed to the default <code>System.err</code> instead of the default
	 * <code>System.out</code>. Set the output stream where all log messages using a {@link Logger} will be printed to.
	 * 
	 * @param u If loggers should use <code>stderr</code> for log messages
	 * @throws SecurityException If a security manager is present and does not allow reassignment of the logger output stream
	 */
	public static void setUseStderr(boolean u) {
		checkLoggerIOPermission();
		if(u)
			LoggerUtil.loggerOut = LoggerUtil.sysErr;
		else
			LoggerUtil.loggerOut = LoggerUtil.sysOut;
	}

	/**
	 * Adds a log listener. A log listener is called every time a log message is generated on any log level, even if the log level is disabled.<br>
	 * <br>
	 * The callback receives two arguments:
	 * <ul>
	 * <li><code>LogLevel</code> - The log level on which the log message was generated.</li>
	 * <li><code>String</code> - The already formatted string, as it is printed to the output stream and the log file. Does not contain control characters for log
	 * coloring.</li>
	 * </ul>
	 * 
	 * @param listener The callback
	 * @throws SecurityException If a security manager is present and does not allow adding log listeners
	 */
	public static void addLogListener(BiConsumer<LogLevel, String> listener) {
		checkPermission(LOG_LISTENER_PERMISSION);
		LoggerUtil.listeners.add(listener);
	}


	protected static synchronized void addLogToBuffer(String s) {
		if(LoggerUtil.logFile != null)
			LoggerUtil.logBuffer.add(s);
		if(LoggerUtil.logBuffer.size() >= LOG_BUFFER_MAX || LoggerUtil.syncFlush)
			LoggerUtil.flushLogBuffer();
	}

	/**
	 * Saves the log buffer to the log file.
	 */
	public static synchronized void flushLogBuffer() {
		if(LoggerUtil.logBuffer.size() > 0 && LoggerUtil.logFile != null){
			try{
				Writer w = new BufferedWriter(new FileWriter(LoggerUtil.logFile, true));
				for(String l : LoggerUtil.logBuffer){
					w.append(l + "\n");
				}
				w.close();
			}catch(IOException e){
				logger.fatal("Error while saving log file: " + e);
			}
		}
		LoggerUtil.logBuffer.clear();
	}

	protected static synchronized void logToStdout(String o) {
		LoggerUtil.loggerOut.println(o);
	}

	protected static synchronized void logToListeners(LogLevel logLevel, String s) {
		for(BiConsumer<LogLevel, String> l : LoggerUtil.listeners)
			l.accept(logLevel, s);
	}


	/**
	 * Searches the log level referenced by the given String, either by log level number or log level name.
	 * 
	 * @param str The string to resolve
	 * @return The log level represented by the given string, or <b>null</b> if no appropriate log level was found
	 */
	public static LogLevel resolveLogLevel(String str) {
		if(str == null)
			return null;
		for(LogLevel l : LogLevel.values()){
			if(l.toString().equals(str) || String.valueOf(l.level()).equals(str))
				return l;
		}
		return null;
	}


	/**
	 * @param syncFlush If the log buffer should be flushed for every message
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static void setSyncFlush(boolean syncFlush) {
		checkLoggerSettingsPermission();
		LoggerUtil.syncFlush = syncFlush;
	}

	/**
	 * Sets the log level to the given value and returns the previous value.
	 * 
	 * @param logLevel The new log level
	 * @return The previous log level
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static LogLevel setLogLevel(LogLevel logLevel) {
		checkLoggerSettingsPermission();
		LogLevel prev = LoggerUtil.logLevel;
		LoggerUtil.logLevel = logLevel;
		return prev;
	}

	/**
	 * 
	 * @return The current log level
	 */
	public static LogLevel getLogLevel() {
		return LoggerUtil.logLevel;
	}
}
