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

/**
 * Provides functions for time-based scheduling, running functions either once or periodically, similar to JavaScript's <tt>setTimeout</tt> and <tt>setInterval</tt>.<br>
 * <br>
 * Tasks are run in a separate background thread. Multiple tasks may run concurrently. This implementation uses an {@link EventQueueExecutor}.<br>
 * <br>
 * Timing may be inaccurate, depending on the platform. The implementation uses {@link Object#wait(long)}.
 */
public class TaskScheduler {

	private List<TimerTask> queue = new ArrayList<>();

	private long idCounter = 0;

	private EventQueueExecutor executor = new EventQueueExecutor(false, "TaskScheduler");

	private boolean running = true;

	/**
	 * Creates a TaskScheduler and starts the background execution thread.
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
	 * @see #timeout(Runnable, long)
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
	 * @param args     Arguments to be passed to the handler
	 * @return The {@link TimerTask} instance of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(TimerTask)}
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
	 */
	private void queue(TimerTask task) {
		synchronized(this.queue){
			int index = this.queue.size();
			for(int i = 0; i < this.queue.size(); i++){
				if(this.queue.get(i).time > task.time){
					index = i;
					break;
				}
			}
			this.queue.add(index, task);
			this.queue.notify();
		}
	}


	/**
	 * Cancels the given timer task.
	 * 
	 * @param tt The {@link TimerTask} to cancel
	 * @return <b>true</b> if the task was found and successfully canceled
	 */
	public boolean clear(TimerTask tt) {
		return clear(tt.id);
	}

	/**
	 * Cancels the given timer task.
	 * 
	 * @param id The id of the {@link TimerTask} to cancel
	 * @return <b>true</b> if the task was found and successfully canceled
	 */
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
				e.printStackTrace();
				break;
			}catch(Exception e){
				e.printStackTrace();
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

	/**
	 * 
	 * @return <b>true</b> if all queued tasks are marked as daemon using {@link TimerTask#daemon()} or if there are no queued tasks.
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
	 * Represents a task managed by a {@link TaskScheduler}.
	 */
	public static class TimerTask {

		private final long id;
		private final Consumer<Object[]> handler;
		private long time;
		private final long period;
		private final Object[] args;

		private boolean daemon;
		private boolean canceled = false;

		protected TimerTask(long id, Consumer<Object[]> handler, long time, long period, Object[] args) {
			this.id = id;
			this.handler = handler;
			this.time = time;
			this.period = period;
			this.args = args;
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

		protected void run() {
			this.handler.accept(this.args);
		}


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
		 * @return The arguments defined for this task
		 */
		public Object[] getArgs() {
			return this.args;
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
