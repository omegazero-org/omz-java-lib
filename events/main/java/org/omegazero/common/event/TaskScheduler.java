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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.omegazero.common.event.task.LambdaTask;

/**
 * Provides functions for time-based scheduling, running functions either once or periodically, similar to JavaScript's <tt>setTimeout</tt> and <tt>setInterval</tt>.
 * <p>
 * Tasks are run in a separate background thread. Multiple tasks may run concurrently. This implementation uses an {@link EventQueueExecutor}.
 * <p>
 * Timing may be inaccurate, depending on the platform. The implementation uses {@link Object#wait(long)}.
 * 
 * @since 2.1
 */
public class TaskScheduler {

	private List<TimerTask> queue = new ArrayList<>();

	private long idCounter = 0;

	private TaskQueueExecutor executor = TaskQueueExecutor.fromSequential().name("TaskScheduler").workerThreads(-1).build();

	private Consumer<Throwable> errorHandler;

	private boolean running = true;

	/**
	 * Creates a TaskScheduler and starts the background execution thread.
	 * 
	 * @since 2.1
	 */
	public TaskScheduler() {
		Thread executionThread = new Thread(){

			@Override
			public void run() {
				TaskScheduler.this.execute(true);
			}
		};
		executionThread.setName("TaskSchedulerThread");
		executionThread.setDaemon(true);
		executionThread.start();
	}


	/**
	 * Schedules a task to be run at the specified <b>timeout</b> relative to the time this function was called.
	 * 
	 * @param handler The handler to be run at the specified <b>timeout</b>
	 * @param timeout The offset in milliseconds when the handler should be called
	 * @return The {@link TimerTask} instance of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(TimerTask)}
	 * @since 2.6
	 * @see #timeout(Consumer, long, Object...)
	 * @see #interval(Runnable, long)
	 * @see #interval(Consumer, long, Object...)
	 */
	public TimerTask timeout(Runnable handler, long timeout) {
		return this.timeout((a) -> {
			handler.run();
		}, timeout);
	}

	/**
	 * Schedules a task to be run at the specified <b>timeout</b> relative to the time this function was called.
	 * 
	 * @param handler The handler to be run at the specified <b>timeout</b>
	 * @param timeout The offset in milliseconds when the handler should be called
	 * @param args    Arguments to be passed to the handler
	 * @return The {@link TimerTask} instance of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(TimerTask)}
	 * @since 2.1
	 * @see #timeout(Runnable, long)
	 * @see #interval(Runnable, long)
	 * @see #interval(Consumer, long, Object...)
	 */
	public TimerTask timeout(Consumer<Object[]> handler, long timeout, Object... args) {
		TimerTask tt = new TimerTask(++this.idCounter, handler, System.currentTimeMillis() + timeout, 0, args);
		queue(tt);
		return tt;
	}

	/**
	 * Schedules a task to be run every <b>interval</b> milliseconds. The task is first run in <b>interval</b> milliseconds relative to the time this function was called.
	 * 
	 * @param handler  The handler to be run at the specified <b>interval</b>
	 * @param interval The time in milliseconds between calls
	 * @return The {@link TimerTask} instance of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(TimerTask)}
	 * @since 2.6
	 * @see #interval(Consumer, long, Object...)
	 * @see #timeout(Runnable, long)
	 * @see #timeout(Consumer, long, Object...)
	 */
	public TimerTask interval(Runnable handler, long interval) {
		return this.interval((a) -> {
			handler.run();
		}, interval);
	}

	/**
	 * Schedules a task to be run every <b>interval</b> milliseconds. The task is first run in <b>interval</b> milliseconds relative to the time this function was called.
	 * 
	 * @param handler  The handler to be run at the specified <b>interval</b>
	 * @param interval The time in milliseconds between calls
	 * @param args     Arguments to be passed to the handler
	 * @return The {@link TimerTask} instance of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(TimerTask)}
	 * @since 2.1
	 * @see #interval(Runnable, long)
	 * @see #timeout(Runnable, long)
	 * @see #timeout(Consumer, long, Object...)
	 */
	public TimerTask interval(Consumer<Object[]> handler, long interval, Object... args) {
		TimerTask tt = new TimerTask(++this.idCounter, handler, System.currentTimeMillis() + interval, interval, args);
		queue(tt);
		return tt;
	}


	/**
	 * Queues the given task.
	 * 
	 * @param task The task to queue
	 * @since 2.1
	 */
	private void queue(TimerTask task) {
		synchronized(this.queue){
			int index = this.queue.size();
			int l = 0;
			int r = this.queue.size() - 2;
			while(l <= r){
				int m = (l + r) / 2;
				TimerTask qtask = this.queue.get(m);
				TimerTask qtaskNext = this.queue.get(m + 1);
				if(task.time > qtaskNext.time){
					l = m + 1;
				}else if(task.time < qtask.time){
					r = m - 1;
				}else{
					index = m + 1;
					break;
				}
			}
			if(r < 0)
				index = 0;
			this.queue.add(index, task);
			this.queue.notifyAll();
		}
	}


	/**
	 * Cancels the given timer task.
	 * 
	 * @param tt The {@link TimerTask} to cancel
	 * @return {@code true} if <b>tt</b> is not {@code null}
	 * @throws ClassCastException If the given parameter is not a {@code TimerTask}
	 * @since 2.9.1
	 * @see #clear(TimerTask)
	 * @see TimerTask#cancel()
	 */
	public boolean clear(Object tt) {
		return this.clear((TimerTask) tt);
	}

	/**
	 * Cancels the given timer task.
	 * 
	 * @param tt The {@link TimerTask} to cancel
	 * @return {@code true} if <b>tt</b> is not {@code null}
	 * @since 2.1
	 * @see TimerTask#cancel()
	 */
	public boolean clear(TimerTask tt) {
		if(tt == null)
			return false;
		synchronized(this.queue){
			tt.canceled = true;
			this.queue.notifyAll();
			return true;
		}
	}

	/**
	 * Cancels the given timer task.
	 * 
	 * @param id The id of the {@link TimerTask} to cancel
	 * @return {@code true} if the task was found and successfully canceled
	 * @since 2.1
	 * @see #clear(TimerTask)
	 * @see TimerTask#cancel()
	 * @deprecated Since 2.9.1, because of poor performance. Use {@link #clear(TimerTask)} or {@link #clear(Object)} instead.
	 */
	@Deprecated
	public boolean clear(long id) {
		synchronized(this.queue){
			for(int i = 0; i < this.queue.size(); i++){
				TimerTask t = this.queue.get(i);
				if(t.id == id){
					t.canceled = true;
					this.queue.notifyAll();
					return true;
				}
			}
			return false;
		}
	}


	private void execute(boolean persistent) {
		while(!persistent || this.running){
			try{
				synchronized(this.queue){
					if(!persistent && isAllDaemon())
						break;
					while(this.queue.size() < 1){
						this.queue.wait();
					}
					executeNext();
				}
			}catch(InterruptedException e){
				break;
			}catch(Exception e){
				this.handleError(e);
			}
		}
	}

	private void executeNext() throws InterruptedException {
		synchronized(this.queue){
			if(this.queue.size() < 1)
				return;
			long time = System.currentTimeMillis();
			TimerTask t = this.queue.get(0);

			if(t.canceled)
				this.queue.remove(0);
			else if(t.time > time)
				this.queue.wait(t.time - time);

			if(this.queue.size() < 1)
				return;
			time = System.currentTimeMillis();
			t = this.queue.get(0);
			if(t.canceled){
				this.queue.remove(0);
			}else if(t.time <= time){
				this.queue.remove(0);
				if(t.period > 0){
					t.time = time + t.period;
					queue(t);
				}
				this.executor.queue((args) -> {
					((TimerTask) args[0]).run();
				}, 0, t);
			}
		}
	}

	private void handleError(Throwable e) {
		if(this.errorHandler != null)
			this.errorHandler.accept(e);
		else
			e.printStackTrace();
	}


	/**
	 * 
	 * @return <b>true</b> if all queued tasks are marked as daemon using {@link TimerTask#daemon()} or if there are no queued tasks
	 * @since 2.1
	 */
	public boolean isAllDaemon() {
		for(int i = 0; i < this.queue.size(); i++){
			TimerTask t = this.queue.get(i);
			if(!t.daemon && !t.canceled){
				return false;
			}
		}
		return true;
	}


	/**
	 * Equivalent to a call to:
	 * 
	 * <pre>
	 * {@link #exit(boolean) exit(false)}
	 * </pre>
	 * 
	 * @since 2.1
	 */
	public void exit() {
		this.exit(false);
	}

	/**
	 * Exits this <b>TaskScheduler</b> by running any remaining tasks (also waiting for ones that are to be run in the future and non-daemon) and exiting the worker
	 * threads.<br>
	 * <br>
	 * If <b>blocking</b> is <code>true</code>, the caller thread is blocked until all remaining non-daemon tasks are run and all worker threads have exited; otherwise, the
	 * shutdown procedure is run by a separate thread.
	 * 
	 * @param blocking Whether the call to this method should block until the shutdown procedure is complete
	 * @since 2.1
	 */
	public void exit(boolean blocking) {
		synchronized(this){
			if(!this.running)
				return;
			this.running = false;
		}
		if(blocking){
			this.exit0();
		}else{
			Thread shutdownThread = new Thread(this::exit0, "TaskSchedulerShutdownThread");
			shutdownThread.start();
		}
	}

	private void exit0() {
		synchronized(this.queue){
			this.queue.notify();
		}
		this.execute(false);
		this.executor.exit(true);
	}


	/**
	 * Sets the error handler that will be called when an error occurs while queuing a task. Also sets the error handler of this <code>TaskScheduler</code>'s
	 * {@link TaskQueueExecutor}.<br>
	 * <br>
	 * If an error occurs while queuing a task and no handler is set, the error is {@linkplain Throwable#printStackTrace() printed to <code>stderr</code>}. For default
	 * behavior when an error occurs while running a task, see {@link TaskQueueExecutor#setErrorHandler(Consumer)}.
	 * 
	 * @param errorHandler The error handler
	 * @since 2.6
	 * @see TaskQueueExecutor#setErrorHandler(Consumer)
	 */
	public void setErrorHandler(Consumer<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
		this.executor.setErrorHandler(errorHandler);
	}


	/**
	 * Represents a task managed by a {@link TaskScheduler}.
	 */
	public class TimerTask extends LambdaTask {

		private final long id;
		private long time;
		private final long period;

		private boolean daemon;
		private boolean canceled = false;

		protected TimerTask(long id, Consumer<Object[]> handler, long time, long period, Object[] args) {
			super(handler, args);
			this.id = id;
			this.time = time;
			this.period = period;
		}


		/**
		 * Cancels this task.
		 * 
		 * Equivalent to a call to:
		 * 
		 * <pre>
		 * taskScheduler.{@link TaskScheduler#clear(TimerTask) clear}(this)
		 * </pre>
		 * 
		 * where <code>taskScheduler</code> is the {@link TaskScheduler} that created this {@link TimerTask}.
		 * 
		 * @return <code>true</code> if the task was found and successfully canceled
		 * @since 2.6
		 * @see TaskScheduler#clear(TimerTask)
		 */
		public boolean cancel() {
			return TaskScheduler.this.clear(this);
		}


		/**
		 * Marks this task as a daemon task. If a task is a daemon task, it will not prevent the {@link TaskScheduler} from exiting.
		 * 
		 * @return This instance
		 * @see TaskScheduler#exit(boolean)
		 * @see #undaemon()
		 */
		public TimerTask daemon() {
			this.daemon = true;
			return this;
		}

		/**
		 * Marks this task as a non-daemon task. Queued non-daemon tasks prevent the {@link TaskScheduler} from exiting.
		 * 
		 * @return This instance
		 * @see TaskScheduler#exit(boolean)
		 * @see #daemon()
		 */
		public TimerTask undaemon() {
			this.daemon = false;
			return this;
		}


		/**
		 * 
		 * @return The {@link TaskScheduler}-instance-wide unique ID of this {@link TimerTask}
		 */
		public long getId() {
			return this.id;
		}

		/**
		 * 
		 * @return The absolute time in milliseconds when this task is scheduled to run
		 */
		public long getTime() {
			return this.time;
		}

		/**
		 * 
		 * @return The interval in milliseconds between running this task
		 */
		public long getPeriod() {
			return this.period;
		}

		/**
		 * 
		 * @return Whether this task was defined as a daemon task
		 * @see #daemon()
		 */
		public boolean isDaemon() {
			return this.daemon;
		}

		/**
		 * 
		 * @return Whether this task is canceled
		 */
		public boolean isCanceled() {
			return this.canceled;
		}
	}
}
