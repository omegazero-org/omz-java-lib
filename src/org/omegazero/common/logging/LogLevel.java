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

public enum LogLevel {
	TRACE("trace", 5, "\u001b[0;90m"), DEBUG("debug", 4, "\u001b[0;37m"), INFO("info", 3, "\u001b[0;97m"), WARN("warn", 2, "\u001b[0;93m"), ERROR("error", 1, "\u001b[0;91m"),
	FATAL("fatal", 0, "\u001b[41;97m");

	private final String label;
	private final int level;
	private final String color;

	private LogLevel(String label, int level, String color) {
		this.label = label;
		this.level = level;
		this.color = color;
	}

	public int level() {
		return level;
	}

	@Override
	public String toString() {
		return label;
	}

	public String color() {
		return color;
	}
}
