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
package org.omegazero.common.event;

import java.util.function.Consumer;

import org.omegazero.common.event.TaskScheduler.TimerTask;

/**
 * Convenience class for globally scheduled tasks using a {@link TaskScheduler}.<br>
 * <br>
 * See methods in {@link TaskScheduler} for detailed information.
 */
public final class Tasks {

	private static TaskScheduler globalInstance = null;

	private Tasks() {
	}

	private static void init() {
		if(globalInstance == null){
			globalInstance = new TaskScheduler();
		}
	}

	public static TimerTask timeout(Consumer<Object[]> handler, long timeout, Object... args) {
		init();
		return globalInstance.timeout(handler, timeout, args);
	}

	public static TimerTask interval(Consumer<Object[]> handler, long interval, Object... args) {
		init();
		return globalInstance.interval(handler, interval, args);
	}

	public static boolean clear(TimerTask tt) {
		init();
		return globalInstance.clear(tt);
	}

	public static boolean clear(long id) {
		init();
		return globalInstance.clear(id);
	}
}
