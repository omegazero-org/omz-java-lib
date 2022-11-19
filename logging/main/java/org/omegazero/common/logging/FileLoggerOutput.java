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

	public static final int SAVE_INTERVAL = PropertyUtil.getInt("org.omegazero.common.logging.saveInterval", 300) * 1000;
	public static final int LOG_BUFFER_MAX = PropertyUtil.getInt("org.omegazero.common.logging.logBufferSize", 1024);

	private String logFile = null;

	private boolean syncFlush = false;

	private List<String> logBuffer = new java.util.ArrayList<>(LOG_BUFFER_MAX);
	private Writer writer = null;

	public FileLoggerOutput(String logFile){
		this.logFile = Objects.requireNonNull(logFile);
		Tasks.interval((args) -> {
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
		if(!syncFlush && this.writer != null){
			try{
				this.writer.close();
			}catch(IOException e){
				// ignore?
			}
			this.writer = null;
		}
	}

	/**
	 * Returns the file name log messages are written to.
	 *
	 * @return The configured log file
	 */
	public String getLogFile() {
		return this.logFile;
	}


	private Writer getWriter() throws IOException {
		if(this.writer != null)
			return this.writer;
		return this.writer = this.getNewWriter();
	}

	private Writer getNewWriter() throws IOException {
		return new BufferedWriter(new FileWriter(this.logFile, true));
	}


	@Override
	public synchronized void writeLine(String line, String markup){
		this.logBuffer.add(line);
		if(this.logBuffer.size() >= LOG_BUFFER_MAX || this.syncFlush)
			this.flush();
	}

	@Override
	public synchronized void flush(){
		if(this.logBuffer.size() > 0){
			if(this.syncFlush){
				try{
					Writer w = this.getWriter();
					for(String l : this.logBuffer){
						w.append(l + "\n");
					}
					w.flush();
				}catch(IOException e){
					this.writer = null;
				}
			}else{
				try(Writer w = this.getNewWriter()){
					for(String l : this.logBuffer){
						w.append(l + "\n");
					}
				}catch(IOException e){
					// ignore?
				}
			}
			this.logBuffer.clear();
		}
	}
}
