/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.omegazero.common.util.Util;

/**
 * The standard {@link Logger} implementation returned by {@link Logger#create()}.
 *
 * @since 2.10
 */
public final class StandardLogger implements Logger {

	private final String fullClassName;
	private final String label;

	StandardLogger(String fullClassName) {
		this(fullClassName, fullClassName);
	}

	StandardLogger(Class<?> creator) {
		this(creator.getName(), creator.getSimpleName());
	}

	StandardLogger(String fullClassName, String label) {
		this.fullClassName = fullClassName;
		this.label = label;
	}


	@Override
	public void log(LogLevel level, Object... obj) {
		boolean logEnabled = this.isLogging(level);
		if(!LoggerUtil.needAllLogMessages() && !logEnabled)
			return;
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
		if(!logEnabled)
			return;
		LoggerUtil.newLogMessage(s, level.color());
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * This is the short name of the class this <code>StandardLogger</code> is bound to.
	 */
	@Override
	public String getLabel(){
		return this.label;
	}


	@Override
	public boolean isLogging(LogLevel level) {
		return !LoggerUtil.isLoggerMuted(this.fullClassName) && LoggerUtil.getLogLevel().level() >= level.level();
	}


	/**
	 * Returns the full name of the class this <code>StandardLogger</code> is bound to.
	 *
	 * @return The full class name
	 * @since 2.5
	 */
	public String getFullClassName() {
		return this.fullClassName;
	}
}
