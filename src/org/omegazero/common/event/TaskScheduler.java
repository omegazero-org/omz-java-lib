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
		Thread shutdownThread = new Thread(){

			@Override
			public void run() {
				TaskScheduler.this.exit();
			}
		};
		shutdownThread.setName("TaskSchedulerShutdownThread");
		Runtime.getRuntime().addShutdownHook(shutdownThread);

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
	 * @param args    Arguments to be passed to the handler
	 * @return The unique id of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(long)}.
	 */
	public TimerTask timeout(Consumer<Object[]> handler, long timeout, Object... args) {
		TimerTask tt = new TimerTask(++idCounter, handler, System.currentTimeMillis() + timeout, 0, args);
		queue(tt);
		return tt;
	}

	/**
	 * Schedules a task to be run every <b>interval</b> milliseconds. The task is first run in <b>interval</b> milliseconds relative to the time this function was called.
	 * 
	 * @param handler  The handler to be run at the specified <b>interval</b>
	 * @param interval The time in milliseconds between calls
	 * @param args     Arguments to be passed to the handler
	 * @return The unique id of this task. May be used as an argument to a subsequent call to {@link TaskScheduler#clear(long)}.
	 */
	public TimerTask interval(Consumer<Object[]> handler, long interval, Object... args) {
		TimerTask tt = new TimerTask(++idCounter, handler, System.currentTimeMillis() + interval, interval, args);
		queue(tt);
		return tt;
	}


	/**
	 * Queues the given task.
	 * 
	 * @param task The task to queue
	 */
	private void queue(TimerTask task) {
		synchronized(queue){
			int index = queue.size();
			for(int i = 0; i < queue.size(); i++){
				if(queue.get(i).time > task.time){
					index = i;
					break;
				}
			}
			queue.add(index, task);
			queue.notify();
		}
	}


	/**
	 * Cancels the given timer task.
	 * 
	 * @param tt The task to cancel
	 * @return <b>true</b> if the task was found and successfully canceled
	 */
	public boolean clear(TimerTask tt) {
		return clear(tt.id);
	}

	/**
	 * Cancels the given timer task.
	 * 
	 * @param id The id of the task to cancel
	 * @return <b>true</b> if the task was found and successfully canceled
	 */
	public boolean clear(long id) {
		synchronized(queue){
			for(int i = 0; i < queue.size(); i++){
				TimerTask t = queue.get(i);
				if(t.id == id){
					t.canceled = true;
					queue.notifyAll();
					return true;
				}
			}
			return false;
		}
	}


	private void execute(boolean persistent) {
		while(!persistent || this.running){
			try{
				synchronized(queue){
					if(!persistent && isAllDaemon())
						break;
					while(queue.size() < 1){
						queue.wait();
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
		synchronized(queue){
			if(queue.size() < 1)
				return;
			long time = System.currentTimeMillis();
			TimerTask t = queue.get(0);

			if(t.canceled)
				queue.remove(0);
			else if(t.time > time)
				queue.wait(t.time - time);

			if(queue.size() < 1)
				return;
			time = System.currentTimeMillis();
			t = queue.get(0);
			if(t.canceled){
				queue.remove(0);
			}else if(t.time <= time){
				queue.remove(0);
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
	 * @return <b>true</b> if all queued tasks are marked as daemon using {@link TimerTask#unref()} or if there are no queued tasks.
	 */
	public boolean isAllDaemon() {
		for(int i = 0; i < queue.size(); i++){
			TimerTask t = queue.get(i);
			if(!t.daemon){
				return false;
			}
		}
		return true;
	}


	/**
	 * Exits this <b>TaskScheduler</b> by running any remaining tasks (also waiting for ones that are to be run in the future) and exiting the worker threads.<br>
	 * The caller thread is blocked until all worker threads have exited.
	 */
	public void exit() {
		if(!this.running)
			return;
		this.running = false;
		synchronized(queue){
			queue.notify();
		}
		TaskScheduler.this.execute(false);
		TaskScheduler.this.executor.exit(true);
	}


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

		public TimerTask daemon() {
			this.daemon = true;
			return this;
		}

		public TimerTask undaemon() {
			this.daemon = false;
			return this;
		}

		protected void run() {
			this.handler.accept(this.args);
		}


		public long getId() {
			return id;
		}

		public long getTime() {
			return time;
		}

		public long getPeriod() {
			return period;
		}

		public Object[] getArgs() {
			return args;
		}

		public boolean isDaemon() {
			return daemon;
		}

		public boolean isCanceled() {
			return canceled;
		}
	}
}
