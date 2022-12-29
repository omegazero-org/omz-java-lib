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
	 * The default interval in seconds between explicit data flushes to the log file.
	 * <p>
	 * <b>Default:</b> {@code 300}
	 */
	public static final int SAVE_INTERVAL = PropertyUtil.getInt("org.omegazero.common.logging.saveInterval", 300) * 1000;
	/**
	 * System property <code>org.omegazero.common.logging.logBufferSize</code>
	 * <p>
	 * The default size of the internal log data buffer.
	 * <p>
	 * <b>Default:</b> {@code 100KB}
	 *
	 * @since 2.11.0
	 */
	public static final int LOG_BUFFER_BYTES = PropertyUtil.getInt("org.omegazero.common.logging.logBufferSize", 100000);
	/**
	 * System property <code>org.omegazero.common.logging.syncFlush</code>
	 * <p>
	 * Sets the default value for {@link #setSyncFlush(boolean) syncFlush}.
	 * <p>
	 * <b>Default:</b> {@code false}
	 *
	 * @since 2.11.0
	 */
	public static final boolean SYNC_FLUSH_DEFAULT = PropertyUtil.getBoolean("org.omegazero.common.logging.syncFlush", false);

	private final String logFile;
	private final int logBufferBytes;

	/**
	 * The output writer.
	 */
	protected PrintWriter writer;

	private boolean syncFlush = SYNC_FLUSH_DEFAULT;

	/**
	 * Creates a new {@code FileLoggerOutput}, writing to the given {@code logFile}.
	 *
	 * @param logFile The name of the output log file
	 * @throws IOException If an IO error occurs while opening the file
	 * @see #FileLoggerOutput(String, int, int)
	 */
	public FileLoggerOutput(String logFile) throws IOException {
		this(logFile, -1, -1);
	}

	/**
	 * Creates a new {@code FileLoggerOutput}, writing to the given {@code logFile}.
	 *
	 * @param logFile The name of the output log file
	 * @param logBufferBytes The size of the internal log data buffer. If this value is nonpositive, the {@link #LOG_BUFFER_BYTES default} is used
	 * @param saveInterval The save interval. If this value is nonpositive, the {@link #SAVE_INTERVAL default} is used
	 * @throws IOException If an IO error occurs while opening the file
	 * @since 2.11.0
	 */
	public FileLoggerOutput(String logFile, int logBufferBytes, int saveInterval) throws IOException {
		this.logFile = Objects.requireNonNull(logFile);
		this.logBufferBytes = logBufferBytes > 0 ? logBufferBytes : LOG_BUFFER_BYTES;

		this.reopen();
		Tasks.I.interval((args) -> {
			this.flush();
		}, saveInterval > 0 ? saveInterval : SAVE_INTERVAL).daemon();
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


	/**
	 * Closes and re-opens the file output stream where data is written to.
	 *
	 * @since 2.11.0
	 */
	protected void reopen() throws IOException {
		if(this.writer != null)
			this.writer.close();
		this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.logFile, true), this.logBufferBytes), false);
	}


	@Override
	public void writeLine(String line, String markup){
		this.writer.println(line);
		if(this.syncFlush)
			this.flush();
	}

	@Override
	public void flush(){
		this.writer.flush();
	}

	@Override
	public void close(){
		this.writer.close();
	}
}
