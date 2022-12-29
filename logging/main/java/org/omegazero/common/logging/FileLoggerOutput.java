/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

import org.omegazero.common.event.Tasks;
import org.omegazero.common.util.PropertyUtil;

/**
 * A {@link LoggerOutput} writing to a file.
 *
 * @since 2.10
 */
public class FileLoggerOutput implements LoggerOutput {

	/**
	 * System property <code>org.omegazero.common.logging.saveInterval</code>
	 * <p>
	 * The interval in seconds between explicit data flushes to the log file.
	 * <p>
	 * <b>Default:</b> {@code 300}
	 */
	public static final int SAVE_INTERVAL = PropertyUtil.getInt("org.omegazero.common.logging.saveInterval", 300) * 1000;
	/**
	 * System property <code>org.omegazero.common.logging.logBufferSize</code>
	 * <p>
	 * The size of the internal log data buffer.
	 * <p>
	 * <b>Default:</b> {@code 100KB}
	 */
	public static final int LOG_BUFFER_BYTES = PropertyUtil.getInt("org.omegazero.common.logging.logBufferSize", 100000);

	private final String logFile;
	private final PrintWriter writer;

	private boolean syncFlush = false;

	/**
	 * Creates a new {@code FileLoggerOutput}, writing to the given {@code logFile}.
	 *
	 * @param logFile The name of the output log file
	 * @throws IOException If an IO error occurs while opening the file
	 */
	public FileLoggerOutput(String logFile) throws IOException {
		this.logFile = Objects.requireNonNull(logFile);
		this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.logFile, true), LOG_BUFFER_BYTES), false);
		Tasks.I.interval((args) -> {
			this.flush();
		}, SAVE_INTERVAL).daemon();
	}


	/**
	 * Sets whether the log buffer should be flushed for every message.
	 *
	 * @param syncFlush {@code true} to synchronously flush the log buffer
	 */
	public void setSyncFlush(boolean syncFlush) {
		this.syncFlush = syncFlush;
	}

	/**
	 * Returns the file name log messages are written to.
	 *
	 * @return The configured log file
	 */
	public String getLogFile() {
		return this.logFile;
	}


	@Override
	public synchronized void writeLine(String line, String markup){
		this.writer.println(line);
		if(this.syncFlush)
			this.flush();
	}

	@Override
	public synchronized void flush(){
		this.writer.flush();
	}

	@Override
	public void close(){
		this.writer.close();
	}
}
