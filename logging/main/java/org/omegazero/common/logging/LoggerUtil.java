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

import java.io.IOException;
import java.io.PrintStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import org.omegazero.common.event.Tasks;
import org.omegazero.common.util.PropertyUtil;

/**
 * Maintains and manages the logging state of <i>omz-java-lib</i> and {@link Logger} instances.
 *
 * @see #init(LogLevel, String)
 * @see LUPermission
 * @since 2.1
 */
public final class LoggerUtil {

	/**
	 * The initial value of {@link System#out}.
	 */
	public static final PrintStream sysOut = System.out;
	/**
	 * The initial value of {@link System#err}.
	 */
	public static final PrintStream sysErr = System.err;

	/**
	 * System property <code>org.omegazero.common.logging.internalDebug</code>
	 * <p>
	 * Enables logging of internal debugging messages to {@code stderr}.
	 * <p>
	 * <b>Default:</b> {@code false}
	 */
	public static final boolean ENABLE_INTERNAL_DEBUG = PropertyUtil.getBoolean("org.omegazero.common.logging.internalDebug", false);

	private static final String[] SKIP_CLASSES = { "sun.reflect", "java.lang.reflect", "jdk.internal.reflect" };

	private static final Logger logger = createLogger();

	private static LogLevel logLevel = LogLevel.INFO;

	private static final Set<String> mutedLoggers = new java.util.HashSet<>();

	private static final List<BiConsumer<LogLevel, String>> listeners = new ArrayList<>();
	private static final List<BiConsumer<LogLevel, String>> listenersFine = new ArrayList<>();

	private static List<LoggerOutput> outputs = new ArrayList<>();

	private LoggerUtil() {
	}


	/**
	 * Initializes the logger, setting the maximum log level to <b>level</b> and the log file name, where log messages will be saved to.
	 * <p>
	 * To reduce disk writes and increase logging speed, by default, log messages will only be saved every 5 minutes, if the logger buffer is full or when
	 * {@link LoggerUtil#close()} is called. This may be disabled using {@link LoggerUtil#setSyncFlush(boolean)} by setting it to {@code true}.
	 * The log buffer may be flushed explicitly using {@link LoggerUtil#flushLogBuffer()}. The save interval may be changed using the
	 * <code>org.omegazero.common.logging.saveInterval</code> system property by setting it to a number representing the time in seconds before this class is used.
	 *
	 * @param level The maximum log level for log messages. Log messages higher than this will be omitted. May be {@code null}, in which case it will be set to
	 *              {@link LogLevel#INFO}
	 * @param file  The file name to save log messages to. May be {@code null} to disable disk saving
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static void init(LogLevel level, String file) {
		new LUPermission("settings", "init", null).check();

		if(level != null)
			LoggerUtil.logLevel = level;
		else
			LoggerUtil.logLevel = LogLevel.INFO;
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: Init with log level " + logLevel + " and file " + file);

		addLoggerOutput(new StdStreamsLoggerOutput());
		try{
			if(file != null)
				addLoggerOutput(new FileLoggerOutput(file));
		}catch(IOException e){
			sysErr.println("LoggerUtil: WARNING: An error occured creating a FileLoggerOutput for '" + file + "': " + e);
		}
	}

	/**
	 * Saves the log buffer and resets the LoggerUtil.
	 *
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static void close() {
		new LUPermission("settings", "close", null).check();
		FileLoggerOutput fout = getLoggerOutputByType(FileLoggerOutput.class);
		if(fout != null){
			logger.info("Saving log to '" + fout.getLogFile() + "'");
		}
		synchronized(outputs){
			for(LoggerOutput o : outputs){
				o.close();
			}
			outputs.clear();
		}
	}


	/**
	 * Creates a new logger bound to the calling class.
	 *
	 * @return The new logger instance
	 * @throws SecurityException If a security manager is present and does not allow creating a new logger instance
	 */
	public static Logger createLogger() {
		return createLogger(0);
	}

	protected static Logger createLogger(int off) {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		StackTraceElement caller = null;
		for(int i = 3 + off; i < st.length; i++){
			String name = st[i].getClassName();
			boolean skip = false;
			for(String s : SKIP_CLASSES){
				if(name.startsWith(s)){
					skip = true;
					break;
				}
			}
			if(!skip){
				caller = st[i];
				break;
			}
		}
		String fullName = caller.getClassName();
		String[] str = fullName.split("\\.");
		String label = str[str.length - 1];
		new LUPermission("logger", "create", fullName).check();
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: New StandardLogger for " + fullName);
		return new StandardLogger(fullName, label);
	}

	protected static Logger createLogger(Class<?> cl) {
		return new StandardLogger(cl.getName(), cl.getSimpleName());
	}


	/**
	 * Mutes the {@link StandardLogger} with the given name, causing the affected logger to no longer print log messages. If a fine log listener is configured using
	 * {@link #addFineLogListener(BiConsumer)}, the logger will continue to generate log messages.
	 *
	 * @param fullClassName The name of the logger, as returned by {@link StandardLogger#getFullClassName()} or {@link Class#getName()}
	 * @return <code>true</code> if the logger was not already muted
	 * @throws SecurityException If a security manager is present and does not allow muting the logger with the given name
	 * @since 2.5
	 * @see #unmuteLogger(String)
	 */
	public static boolean muteLogger(String fullClassName) {
		new LUPermission("logger", "mute", fullClassName).check();
		return mutedLoggers.add(fullClassName);
	}

	/**
	 * Unmutes the {@link StandardLogger} with the given name, reversing any mute operation by a previous call to {@link #muteLogger(String)}.
	 *
	 * @param fullClassName The name of the logger, as returned by {@link StandardLogger#getFullClassName()} or {@link Class#getName()}
	 * @return <code>true</code> if the logger was not muted
	 * @throws SecurityException If a security manager is present and does not allow unmuting the logger with the given name
	 * @since 2.5
	 * @see #muteLogger(String)
	 */
	public static boolean unmuteLogger(String fullClassName) {
		new LUPermission("logger", "unmute", fullClassName).check();
		return mutedLoggers.remove(fullClassName);
	}

	/**
	 * Returns whether the logger with the given name is muted due to a previous call to {@link #muteLogger(String)}
	 *
	 * @param fullClassName The name of the logger, as returned by {@link StandardLogger#getFullClassName()} or {@link Class#getName()}
	 * @return <code>true</code> if the logger is muted
	 * @since 2.5
	 */
	public static boolean isLoggerMuted(String fullClassName) {
		return mutedLoggers.contains(fullClassName);
	}


	/**
	 * Redirects the <code>System.out</code> and <code>System.err</code> to Logger streams, causing messages printed using <code>System.out.[...]</code> or
	 * <code>System.err.[...]</code> to be formatted to standard logger format and printed with log level {@link LogLevel#INFO}.
	 *
	 * @throws SecurityException If a security manager is present and does not allow reassignment of the standard or logger output streams
	 */
	public static void redirectStandardOutputStreams() {
		new LUPermission("io", "redirectStd", null).check();
		System.setOut(new PrintStream(new LoggerOutputStream("stdout")));
		System.setErr(new PrintStream(new LoggerOutputStream("stderr")));
	}

	/**
	 * Adds a log listener. A log listener is called every time a log message is generated on an enabled log level. To receive all log messages regardless of the configured
	 * log level, use {@link #addFineLogListener(BiConsumer)} instead.
	 * <p>
	 * The callback receives two arguments:
	 * <ul>
	 * <li><code>LogLevel</code> - The log level on which the log message was generated.</li>
	 * <li><code>String</code> - The already formatted string, as it is printed to the output stream and the log file. Does not contain control characters for log
	 * coloring.</li>
	 * </ul>
	 *
	 * @param listener The callback
	 * @throws SecurityException If a security manager is present and does not allow adding log listeners
	 * @see #addFineLogListener(BiConsumer)
	 */
	public static void addLogListener(BiConsumer<LogLevel, String> listener) {
		new LUPermission("logListener", "addRegular", null).check();
		synchronized(listeners){
			listeners.add(listener);
		}
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: Added log listener " + listener);
	}

	/**
	 * Same as {@link #addLogListener(BiConsumer)}, except that listeners added here will receive <i>all</i> log messages, regardless of the configured log level. Note that
	 * this may slow down the application because all log messages need to be generated, instead of only ones below the configured log level.
	 *
	 * @param listener The callback
	 * @throws SecurityException If a security manager is present and does not allow adding log listeners
	 * @since 2.3
	 * @see #addLogListener(BiConsumer)
	 */
	public static void addFineLogListener(BiConsumer<LogLevel, String> listener) {
		new LUPermission("logListener", "addFine", null).check();
		synchronized(listenersFine){
			listenersFine.add(listener);
		}
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: Added fine log listener " + listener);
	}


	/**
	 * Calls {@link LoggerOutput#flush()} on all configured {@link LoggerOutput}s.
	 */
	public static void flushLogBuffer() {
		synchronized(outputs){
			for(LoggerOutput o : outputs){
				o.flush();
			}
		}
	}

	protected static void newLogMessage(String line, String markup) {
		synchronized(outputs){
			for(LoggerOutput o : outputs){
				o.writeLine(line, markup);
			}
		}
	}

	protected static synchronized void logToListeners(LogLevel logLevel, String s) {
		if(LoggerUtil.logLevel.level() >= logLevel.level()){
			synchronized(listeners){
				for(BiConsumer<LogLevel, String> l : listeners)
					l.accept(logLevel, s);
			}
		}
		synchronized(listenersFine){
			for(BiConsumer<LogLevel, String> l : listenersFine)
				l.accept(logLevel, s);
		}
	}

	protected static boolean needAllLogMessages() {
		return LoggerUtil.listenersFine.size() > 0;
	}


	/**
	 * Adds the given {@link LoggerOutput} to the list of configured outputs.
	 *
	 * @param output The {@link LoggerOutput}
	 * @since 2.10
	 */
	public static void addLoggerOutput(LoggerOutput output){
		new LUPermission("loggerOutput", "add", output).check();
		synchronized(outputs){
			outputs.add(output);
		}
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: Added logger output " + output);
	}

	/**
	 * Removes the given {@link LoggerOutput} from the list of configured outputs.
	 *
	 * @param output The {@link LoggerOutput}
	 * @return {@code true} if the instance was found and removed
	 * @since 2.10
	 */
	public static boolean removeLoggerOutput(LoggerOutput output){
		new LUPermission("loggerOutput", "remove", output).check();
		boolean f;
		synchronized(outputs){
			f = outputs.remove(output);
		}
		if(ENABLE_INTERNAL_DEBUG && f)
			sysErr.println("LoggerUtil: Removed logger output " + output);
		return f;
	}

	/**
	 * Returns a configured {@link LoggerOutput} with the given type or a subtype thereof. If multiple {@code LoggerOutput}s match, it is undefined which instance is returned.
	 *
	 * @param cl The {@link LoggerOutput} type to search for
	 * @return The {@link LoggerOutput} instance, or {@code null} if no matching instance was found
	 * @since 2.10
	 * @see #getLoggerOutputsByType(Class)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LoggerOutput> T getLoggerOutputByType(Class<T> cl){
		synchronized(outputs){
			for(LoggerOutput o : outputs){
				if(cl.isAssignableFrom(o.getClass()))
					return (T) o;
			}
			return null;
		}
	}

	/**
	 * Returns all configured {@link LoggerOutput}s with the given type or a subtype thereof.
	 *
	 * @param cl The {@link LoggerOutput} type to search for
	 * @return The {@link LoggerOutput} instances, may be empty
	 * @since 2.10
	 * @see #getLoggerOutputByType(Class)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LoggerOutput> List<T> getLoggerOutputsByType(Class<T> cl){
		synchronized(outputs){
			List<T> outs = new ArrayList<>();
			for(LoggerOutput o : outputs){
				if(cl.isAssignableFrom(o.getClass()))
					outs.add((T) o);
			}
			return outs;
		}
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
	 * If set to {@code true}, all log messages created using a {@link StandardLogger} will be printed to the default <code>System.err</code> instead of the default
	 * <code>System.out</code>. Set the output stream where all log messages using a {@link StandardLogger} will be printed to.
	 *
	 * @param u If loggers should use <code>stderr</code> for log messages
	 * @throws SecurityException If a security manager is present and does not allow reassignment of the logger output stream
	 * @deprecated Since 2.10, use
			<code>{@link #getLoggerOutputByType(Class) getLoggerOutputByType}({@link StdStreamsLoggerOutput}.class).{@link StdStreamsLoggerOutput#setUseStderr setUseStderr}(u)</code> instead
	 */
	@Deprecated
	public static void setUseStderr(boolean u) {
		new LUPermission("io", "useStderr", u).check();
		getLoggerOutputByType(StdStreamsLoggerOutput.class).setUseStderr(u);
	}

	/**
	 * Sets whether the log buffer should be flushed for every message.
	 *
	 * @param syncFlush {@code true} to synchronously flush the log buffer
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 * @deprecated Since 2.10, use
			<code>{@link #getLoggerOutputByType(Class) getLoggerOutputByType}({@link FileLoggerOutput}.class).{@link FileLoggerOutput#setSyncFlush setSyncFlush}(syncFlush)</code> instead
	 */
	@Deprecated
	public static void setSyncFlush(boolean syncFlush) {
		new LUPermission("settings", "setSyncFlush", syncFlush).check();
		FileLoggerOutput o = getLoggerOutputByType(FileLoggerOutput.class);
		if(o != null)
			o.setSyncFlush(syncFlush);
	}

	/**
	 * Returns the file name log messages are written to.
	 *
	 * @return The configured log file, or {@code null} if no log file is used
	 * @since 2.5
	 * @deprecated Since 2.10, use
			<code>{@link #getLoggerOutputByType(Class) getLoggerOutputByType}({@link FileLoggerOutput}.class).{@link FileLoggerOutput#getLogFile getLogFile}()</code> instead
	 */
	@Deprecated
	public static String getLogFile() {
		FileLoggerOutput o = getLoggerOutputByType(FileLoggerOutput.class);
		if(o != null)
			return o.getLogFile();
		else
			return null;
	}


	/**
	 * Sets the log level to the given value and returns the previous value.
	 *
	 * @param logLevel The new log level
	 * @return The previous log level
	 * @throws SecurityException If a security manager is present and does not allow changing logger settings
	 */
	public static LogLevel setLogLevel(LogLevel logLevel) {
		new LUPermission("settings", "setLogLevel", logLevel).check();
		LogLevel prev = LoggerUtil.logLevel;
		LoggerUtil.logLevel = logLevel;
		if(ENABLE_INTERNAL_DEBUG)
			sysErr.println("LoggerUtil: Log level changed to " + logLevel + " (from " + prev + ")");
		return prev;
	}

	/**
	 * Returns the current log level.
	 *
	 * @return The current log level
	 */
	public static LogLevel getLogLevel() {
		return LoggerUtil.logLevel;
	}


	/**
	 * Represents a permission checked by the security manager when an operation is performed that changes the logging state.
	 * <p>
	 * The following permissions are checked:
	 *
	 * <table>
	 * <tr><th>name</th><th>action</th><th>attachment</th><th>Checked by</th></tr>
	 * <tr><td>settings</td><td>init</td><td></td><td>{@link LoggerUtil#init(LogLevel, String)}</td></tr>
	 * <tr><td>settings</td><td>close</td><td></td><td>{@link LoggerUtil#close()}</td></tr>
	 * <tr><td>settings</td><td>setSyncFlush</td><td>The new boolean value</td><td>{@link LoggerUtil#setSyncFlush(boolean)}</td></tr>
	 * <tr><td>settings</td><td>setLogLevel</td><td>The new {@link LogLevel}</td><td>{@link LoggerUtil#setLogLevel(LogLevel)}</td></tr>
	 * <tr><td>io</td><td>redirectStd</td><td></td><td>{@link LoggerUtil#redirectStandardOutputStreams()}</td></tr>
	 * <tr><td>io</td><td>useStderr</td><td>The new boolean value</td><td>{@link LoggerUtil#setUseStderr(boolean)}</td></tr>
	 * <tr><td>logListener</td><td>addRegular</td><td></td><td>{@link LoggerUtil#addLogListener(BiConsumer)}</td></tr>
	 * <tr><td>logListener</td><td>addFine</td><td></td><td>{@link LoggerUtil#addFineLogListener(BiConsumer)}</td></tr>
	 * <tr><td>logger</td><td>create</td><td>Full class name of the logger</td><td>{@link LoggerUtil#createLogger()}</td></tr>
	 * <tr><td>logger</td><td>mute</td><td>Full class name of the logger</td><td>{@link LoggerUtil#muteLogger(String)}</td></tr>
	 * <tr><td>logger</td><td>unmute</td><td>Full class name of the logger</td><td>{@link LoggerUtil#unmuteLogger(String)}</td></tr>
	 * <tr><td>loggerOutput</td><td>add</td><td>The instance</td><td>{@link LoggerUtil#addLoggerOutput(LoggerOutput)}</td></tr>
	 * <tr><td>loggerOutput</td><td>remove</td><td>The instance</td><td>{@link LoggerUtil#removeLoggerOutput(String)}</td></tr>
	 * </table>
	 * <p>
	 * All permissions are an instance of this class. <b>name</b> is the string returned by {@link #getName()}, <b>action</b> is a string returned by {@link #getActions()},
	 * <b>attachment</b> any object involved in the checked operation returned by {@link #getAttachment()}.
	 *
	 * @since 2.5
	 */
	public static class LUPermission extends Permission implements java.io.Serializable {

		private static final long serialVersionUID = 1L;

		private String actions;
		private Object attachment;

		protected LUPermission(String name, String actions, Object attachment) {
			super(Objects.requireNonNull(name));
			this.actions = Objects.requireNonNull(actions);
			this.attachment = attachment;
		}


		@Override
		public boolean implies(Permission permission) {
			return false;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null || !(obj instanceof LUPermission))
				return false;
			LUPermission p = (LUPermission) obj;
			return super.getName().equals(p.getName()) && this.actions.equals(p.actions) && Objects.equals(this.attachment, p.attachment);
		}

		@Override
		public int hashCode() {
			return super.getName().hashCode() + this.actions.hashCode() + (this.attachment != null ? this.attachment.hashCode() : 0);
		}

		@Override
		public String getActions() {
			return this.actions;
		}

		public Object getAttachment() {
			return this.attachment;
		}


		public void check() {
			SecurityManager sm = System.getSecurityManager();
			if(sm != null)
				sm.checkPermission(this);
		}
	}
}
