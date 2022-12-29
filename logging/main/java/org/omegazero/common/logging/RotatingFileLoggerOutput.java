/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.omegazero.common.event.Tasks;
import org.omegazero.common.util.PropertyUtil;
import org.omegazero.common.util.Util;

/**
 * A {@link FileLoggerOutput} with support for log rotation.
 *
 * @since 2.11.0
 */
public class RotatingFileLoggerOutput extends FileLoggerOutput {

	/**
	 * System property <code>org.omegazero.common.logging.rotation.maxFileSize</code>
	 * <p>
	 * The default maximum file size for rotation.
	 * <p>
	 * <b>Default:</b> {@code 0} (log rotation disabled)
	 */
	public static final long MAX_FILE_SIZE_DEFAULT = PropertyUtil.getLong("org.omegazero.common.logging.rotation.maxFileSize", -1);
	/**
	 * System property <code>org.omegazero.common.logging.rotation.checkTimeout</code>
	 * <p>
	 * The time in seconds between rotation checks.
	 * <p>
	 * <b>Default:</b> {@code 60}
	 */
	public static final long CHECK_TIMEOUT = PropertyUtil.getInt("org.omegazero.common.logging.rotation.checkTimeout", 60) * 1000L;

	private final Object rotateLock = new Object();
	private final Object writeLock = new Object();

	private final long maxFileSize;

	private final Path logFilePath;
	private final Path logDirectory;
	private final String logFileName;

	private long lastCheck;
	private long writtenSinceLastCheck;

	/**
	 * Creates a new {@code RotatingFileLoggerOutput}, writing to the given {@code logFile}.
	 *
	 * @param logFile The name of the output log file
	 * @param maxFileSize The maximum file size for rotation. If this value is negative, the {@link #MAX_FILE_SIZE_DEFAULT default} is used; if {@code 0}, log rotation is disabled
	 * @throws IOException If an IO error occurs while opening the file
	 * @see #RotatingFileLoggerOutput(String, long, int, int)
	 */
	public RotatingFileLoggerOutput(String logFile, long maxFileSize) throws IOException {
		this(logFile, -1, -1, -1);
	}

	/**
	 * Creates a new {@code RotatingFileLoggerOutput}, writing to the given {@code logFile}.
	 * <p>
	 * The maximum file size parameter is not a hard limit on the file size of log files. If the log file exceeds this size, it is rotated, but the file may end up larger than this parameter,
	 * especially if there is a lot of log output in a short time.
	 *
	 * @param logFile The name of the output log file
	 * @param maxFileSize The maximum file size for rotation. If this value is negative, the {@link #MAX_FILE_SIZE_DEFAULT default} is used; if {@code 0}, log rotation is disabled
	 * @param logBufferBytes The size of the internal log data buffer
	 * @param saveInterval The save interval
	 * @throws IOException If an IO error occurs while opening the file
	 * @see FileLoggerOutput#FileLoggerOutput(String, int, int)
	 */
	public RotatingFileLoggerOutput(String logFile, long maxFileSize, int logBufferBytes, int saveInterval) throws IOException {
		super(logFile, logBufferBytes, saveInterval);
		this.maxFileSize = maxFileSize >= 0 ? maxFileSize : MAX_FILE_SIZE_DEFAULT;

		this.logFilePath = Paths.get(logFile).toAbsolutePath();
		this.logDirectory = this.logFilePath.getParent();
		this.logFileName = this.logFilePath.getFileName().toString();
	}


	private void checkRotateAsync(){
		if(this.maxFileSize <= 0 || this.maxFileSize == Long.MAX_VALUE)
			return;
		long time = System.nanoTime();
		synchronized(this){
			if(time - this.lastCheck < CHECK_TIMEOUT && this.writtenSinceLastCheck < this.maxFileSize)
				return;
			this.lastCheck = time;
			this.writtenSinceLastCheck = 0;
		}
		Tasks.async(this::checkRotate);
	}

	private void checkRotate(){
		try{
			synchronized(this.rotateLock){
				long logFileSize = Files.size(this.logFilePath);
				if(logFileSize < this.maxFileSize)
					return;
				if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
					LoggerUtil.sysErr.println("RotatingFileLoggerOutput: Calling rotate (log file has " + logFileSize + "/" + this.maxFileSize + " bytes)");
				this.rotate(logFileSize);
			}
		}catch(IOException e){
			if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
				LoggerUtil.sysErr.println("RotatingFileLoggerOutput: checkRotate for '" + this.logFilePath + "' failed: " + e);
		}
	}

	private Path getLogFilePath(int index){
		if(index < 0)
			return this.logFilePath;
		return this.logDirectory.resolve(this.logFileName + "." + index);
	}

	private void rotate(long logFileSize) throws IOException {
		assert Thread.holdsLock(this.rotateLock);
		int maxNum = -1;
		while(Files.exists(this.getLogFilePath(maxNum + 1)))
			maxNum++;
		for(int i = maxNum; i >= 0; i--){
			Path newf = this.getLogFilePath(i + 1);
			Path oldf = this.getLogFilePath(i);
			Files.move(oldf, newf);
		}
		synchronized(this.writeLock){
			super.writer.close();
			Files.move(this.logFilePath, this.getLogFilePath(0));
			super.reopen();
			super.writer.println("-- Log file rotated (" + (logFileSize / 1000) + "/" + (this.maxFileSize / 1000) + "KB) at " + Util.getFormattedTime() + " --");
		}
	}


	@Override
	public void writeLine(String line, String markup){
		synchronized(this.writeLock){
			super.writeLine(line, markup);
		}
		this.writtenSinceLastCheck += line.length();
		this.checkRotateAsync();
	}

	@Override
	public void flush(){
		synchronized(this.writeLock){
			super.flush();
		}
		this.checkRotateAsync();
	}
}
