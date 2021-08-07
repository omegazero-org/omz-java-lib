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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.omegazero.common.util.Util;

public final class Logger {

	private String label;

	protected Logger(String label) {
		this.label = label;
	}


	public void trace(Object... obj) {
		this.log(LogLevel.TRACE, obj);
	}

	public void debug(Object... obj) {
		this.log(LogLevel.DEBUG, obj);
	}

	public void info(Object... obj) {
		this.log(LogLevel.INFO, obj);
	}

	public void warn(Object... obj) {
		this.log(LogLevel.WARN, obj);
	}

	public void error(Object... obj) {
		this.log(LogLevel.ERROR, obj);
	}

	public void fatal(Object... obj) {
		this.log(LogLevel.FATAL, obj);
	}


	public void log(LogLevel level, Object[] obj) {
		StringBuilder sb = new StringBuilder(32);
		sb.append(Util.getFormattedTime()).append(' ');
		sb.append('[').append(level).append(']').append(' ');
		sb.append('[').append(Thread.currentThread().getName()).append(']').append(' ');
		sb.append('[').append(this.label).append(']').append(' ');
		for(Object o : obj){
			if(o instanceof Throwable){
				Throwable t = (Throwable) o;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				t.printStackTrace(ps);
				sb.append(baos.toString());
				ps.close();
			}else
				sb.append(o);
		}
		String s = sb.toString();
		LoggerUtil.logToListeners(level, s);
		if(LoggerUtil.getLogLevel().level() < level.level())
			return;
		LoggerUtil.logToStdout(level.color() + s + "\u001b[0m");
		LoggerUtil.addLogToBuffer(s);
	}


	/**
	 * Utility function to check if debugging is enabled.
	 * 
	 * @return <code>true</code> if the current log level is {@link LogLevel#DEBUG} or higher
	 * @since 2.3
	 */
	public boolean debug() {
		return LoggerUtil.getLogLevel().level() >= LogLevel.DEBUG.level();
	}


	/**
	 * Creates a new logger bound to the calling class.<br>
	 * <br>
	 * This function is equivalent to {@link LoggerUtil#createLogger()}.
	 * 
	 * @return The new logger instance
	 * @since 2.3
	 */
	public static Logger create() {
		return LoggerUtil.createLogger(0);
	}
}
