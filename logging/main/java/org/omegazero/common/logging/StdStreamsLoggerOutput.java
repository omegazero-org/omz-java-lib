/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

import java.io.PrintStream;

/**
 * A {@link LoggerOutput} writing to <i>stdout</i> or <i>stderr</i>.
 *
 * @since 2.10
 */
public class StdStreamsLoggerOutput implements LoggerOutput {

	private PrintStream outStream = LoggerUtil.sysOut;


	/**
	 * Set the output stream where all log messages written to this {@code LoggerOutput} will be printed to.
	 * If set to {@code true}, all log messages will be printed to the default {@code System.err} instead of the default {@code System.out}.
	 *
	 * @param useStderr If loggers should use {@code stderr} for log messages
	 */
	public void setUseStderr(boolean useStderr) {
		if(useStderr)
			this.outStream = LoggerUtil.sysErr;
		else
			this.outStream = LoggerUtil.sysOut;
	}


	@Override
	public void writeLine(String line, String markup){
		this.outStream.println(markup + line + "\u001b[0m");
	}

	@Override
	public void flush(){
		this.outStream.flush();
	}
}
