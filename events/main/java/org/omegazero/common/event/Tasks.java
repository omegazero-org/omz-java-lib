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
 * Convenience class for globally scheduled tasks using a {@link TaskScheduler}.
 * <p>
 * This class uses a single {@link TaskScheduler} instance, accessible through the constant {@link #I}.
 * 
 * @since 2.1
 */
public final class Tasks {

	/**
	 * The {@link TaskScheduler} instance.
	 *
	 * @since 2.9.1
	 * @see #TASKS
	 */
	public static final TaskScheduler I;

	/**
	 * The {@link TaskScheduler} instance, an alias of {@link #I}.
	 * <p>
	 * This constant may be used instead of {@link #I} in a static import, because it has a more descriptive name.
	 *
	 * @since 2.9.1
	 */
	public static final TaskScheduler TASKS;


	private Tasks() {
	}


	/**
	 * See {@link TaskScheduler#timeout(Runnable, long)}.
	 * 
	 * @param handler The handler
	 * @param timeout The timeout in milliseconds
	 * @return The {@link TimerTask} instance
	 * @since 2.6
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static TimerTask timeout(Runnable handler, long timeout) {
		return I.timeout(handler, timeout);
	}

	/**
	 * See {@link TaskScheduler#timeout(Consumer, long, Object...)}.
	 * 
	 * @param handler The handler
	 * @param timeout The timeout in milliseconds
	 * @param args    Handler arguments
	 * @return The {@link TimerTask} instance
	 * @since 2.1
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static TimerTask timeout(Consumer<Object[]> handler, long timeout, Object... args) {
		return I.timeout(handler, timeout, args);
	}

	/**
	 * See {@link TaskScheduler#interval(Runnable, long)}.
	 * 
	 * @param handler The handler
	 * @param timeout The interval in milliseconds
	 * @return The {@link TimerTask} instance
	 * @since 2.6
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static TimerTask interval(Runnable handler, long timeout) {
		return I.interval(handler, timeout);
	}

	/**
	 * See {@link TaskScheduler#interval(Consumer, long, Object...)}.
	 * 
	 * @param handler The handler
	 * @param interval The interval in milliseconds
	 * @param args    Handler arguments
	 * @return The {@link TimerTask} instance
	 * @since 2.1
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static TimerTask interval(Consumer<Object[]> handler, long interval, Object... args) {
		return I.interval(handler, interval, args);
	}

	/**
	 * See {@link TaskScheduler#clear(TimerTask)}.
	 * 
	 * @param tt The {@link TimerTask} to cancel
	 * @return {@code true} if <b>tt</b> is not {@code null}
	 * @since 2.1
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static boolean clear(TimerTask tt) {
		return I.clear(tt);
	}

	/**
	 * See {@link TaskScheduler#clear(long)}.
	 * 
	 * @param id The id of the {@link TimerTask} to cancel
	 * @return {@code true} if the task was found and successfully canceled
	 * @since 2.1
	 * @deprecated Since 2.9.1, use {@link #I}
	 */
	@Deprecated
	public static boolean clear(long id) {
		return I.clear(id);
	}

	/**
	 * Queues the given {@code Runnable} to be run asychronously, as soon as possible.
	 * <p>
	 * This method calls {@link TaskScheduler#timeout(Runnable, long)} with a timeout value of {@code 0}. Usage of this method should be kept at a minimum, since the worker threads executing
	 * handlers passed to this method are shared with other timer tasks. For more resource- or IO-intensive tasks, a dedicated {@code TaskQueueExecutor} should be used instead.
	 *
	 * @param handler The handler
	 * @since 2.11.0
	 * @see #async(Consumer, Object...)
	 */
	public static void async(Runnable handler){
		I.timeout(handler, 0);
	}

	/**
	 * Queues the given {@code Consumer} with the given arguments to be run asychronously, as soon as possible.
	 * <p>
	 * See {@link #async(Runnable)} for usage hints regarding the {@code async} methods.
	 *
	 * @param handler The handler
	 * @param args Arguments to be passed to the handler
	 * @since 2.11.0
	 * @see #async(Runnable)
	 */
	public static void async(Consumer<Object[]> handler, Object... args){
		I.timeout(handler, 0);
	}

	/**
	 * See {@link TaskScheduler#exit()}. Called with {@link #I}.
	 */
	public static void exit() {
		I.exit();
	}

	/**
	 * See {@link TaskScheduler#setErrorHandler(Consumer)}. Called with {@link #I}.
	 * 
	 * @param errorHandler The error handler
	 * @since 2.6
	 */
	public static void setErrorHandler(Consumer<Throwable> errorHandler) {
		I.setErrorHandler(errorHandler);
	}


	static{
		I = new TaskScheduler();
		TASKS = I;
	}
}
