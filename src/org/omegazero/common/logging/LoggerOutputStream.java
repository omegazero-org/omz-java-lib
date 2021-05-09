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
import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {

	private final Logger loggerInstance;

	private final int[] buf = new int[1024];
	private int bufLen = 0;

	protected LoggerOutputStream(String name) {
		this.loggerInstance = new Logger(name);
	}


	public synchronized void writeOut() {
		String text = new String(this.buf, 0, this.bufLen);
		this.loggerInstance.info(text);
		this.bufLen = 0;
	}


	@Override
	public synchronized void write(int b) throws IOException {
		if(b == '\n'){
			this.writeOut();
		}else if(b != '\r'){
			this.buf[this.bufLen++] = b;
			if(this.bufLen >= this.buf.length)
				this.writeOut();
		}
	}

	@Override
	public void flush() {
		if(this.bufLen > 0)
			this.writeOut();
	}
}
