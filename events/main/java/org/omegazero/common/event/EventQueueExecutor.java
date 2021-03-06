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
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.omegazero.common.event.task.Task;

/**
 * Executor for {@link EventQueue}s.
 * 
 * @deprecated Since 2.6. Use a {@link TaskQueueExecutor} with a {@link java.util.concurrent.BlockingQueue} instead.
 */
@Deprecated
public class EventQueueExecutor extends EventQueue {

	private static HashMap<String, Integer> threadCounter = new HashMap<>();

	private final int maxThreads;
	private final boolean daemon;
	private final String threadName;

	private List<WorkerThread> workerThreads = new ArrayList<WorkerThread>();
	private int workingThreads = 0;

	private Consumer<Throwable> errorHandler;

	private boolean running = true;

	/**
	 * Default values:<br>
	 * <b>maxThreads</b>: Number of processors<br>
	 * <b>daemon</b>: true<br>
	 * <b>threadName</b>: "EventQueueExecutor"
	 * 
	 * @see EventQueueExecutor#EventQueueExecutor(int, boolean, String)
	 */
	public EventQueueExecutor() {
		this(Runtime.getRuntime().availableProcessors(), true);
	}

	/**
	 * Default values:<br>
	 * <b>maxThreads</b>: Number of processors<br>
	 * <b>threadName</b>: "EventQueueExecutor"
	 * 
	 * @see EventQueueExecutor#EventQueueExecutor(int, boolean, String)
	 */
	public EventQueueExecutor(boolean daemon) {
		this(Runtime.getRuntime().availableProcessors(), daemon);
	}

	/**
	 * Default values:<br>
	 * <b>daemon</b>: true<br>
	 * <b>threadName</b>: "EventQueueExecutor"
	 * 
	 * @see EventQueueExecutor#EventQueueExecutor(int, boolean, String)
	 */
	public EventQueueExecutor(int maxThreads) {
		this(maxThreads, true);
	}

	/**
	 * Default values:<br>
	 * <b>threadName</b>: "EventQueueExecutor"
	 * 
	 * @see EventQueueExecutor#EventQueueExecutor(int, boolean, String)
	 */
	public EventQueueExecutor(int maxThreads, boolean daemon) {
		this(maxThreads, daemon, "EventQueueExecutor");
	}

	/**
	 * Default values:<br>
	 * <b>maxThreads</b>: Number of processors<br>
	 * <b>threadName</b>: "EventQueueExecutor"
	 * 
	 * @see EventQueueExecutor#EventQueueExecutor(int, boolean, String)
	 */
	public EventQueueExecutor(boolean daemon, String threadName) {
		this(Runtime.getRuntime().availableProcessors(), daemon, threadName);
	}

	/**
	 * Creates a new EventQueue with built-in executor threads that call {@link EventQueue#execute()}. Additionally, if a task is taking too long to complete and another task
	 * was queued, another worker thread will be spawned to concurrently execute the new task.<br>
	 * <br>
	 * If <b>daemon</b> is true, the worker threads will be set as daemon threads. Otherwise, {@link EventQueueExecutor#exit()} will need to be explicitly called to complete
	 * remaining tasks and exit the worker threads.
	 * 
	 * @param maxThreads The maximum number of worker threads to spawn
	 * @param daemon     If worker threads should be set as daemon threads
	 * @param threadName The name of the worker threads, appended with a unique number
	 */
	public EventQueueExecutor(int maxThreads, boolean daemon, String threadName) {
		this.maxThreads = maxThreads;
		this.daemon = daemon;
		this.threadName = threadName;
	}


	// only need to override this methods because other overloaded methods use this method too
	@Override
	public void queue(Task task) {
		if(!this.running)
			throw new IllegalStateException("Tried to queue task but EventQueueExecutor is no longer running");
		super.queue(task);
		this.checkWorkers();
	}


	/**
	 * Ensures that there are enough worker threads available for executing queued tasks. <br>
	 * If not, and the number of running threads is lower than maxThreads, a new worker thread will be created.
	 */
	public synchronized void checkWorkers() {
		if(!this.running)
			return;
		if(super.isTaskQueued() && this.workingThreads >= this.workerThreads.size() && this.workerThreads.size() < this.maxThreads){
			WorkerThread thread = new WorkerThread();
			thread.start();
			this.workerThreads.add(thread);
		}
	}


	/**
	 * Default values:<br>
	 * <b>blocking</b>: false
	 * 
	 * @see EventQueueExecutor#exit(boolean)
	 */
	public void exit() {
		this.exit(false);
	}

	/**
	 * Notifies worker threads that executor is exiting, causing them to complete running all tasks.
	 * 
	 * @param blocking If <code>true</code>, block the calling thread until all worker threads have exited
	 */
	public void exit(boolean blocking) {
		synchronized(this){
			if(!this.running)
				return;
			this.running = false;
			for(WorkerThread thread : this.workerThreads){
				if(!thread.executing)
					thread.interrupt();
			}
			this.notifyAll();
		}
		if(blocking){
			for(WorkerThread thread : this.workerThreads){
				try{
					thread.join();
				}catch(InterruptedException e){
					e.printStackTrace();
					return;
				}
			}
		}
	}


	/**
	 * 
	 * @return The maximum number of worker threads allowed to be created
	 */
	public int getMaxThreads() {
		return this.maxThreads;
	}

	/**
	 * 
	 * @return Number of running worker threads
	 */
	public int getWorkerThreadCount() {
		return this.workerThreads.size();
	}

	/**
	 * Sets the error handler that will be called when an error occurs while executing a task in any of the worker threads.<br>
	 * <br>
	 * If this handler is not set, the worker thread will exit, otherwise, the handler may choose to ignore the error and continue.
	 * 
	 * @param errorHandler The error handler
	 */
	public void setErrorHandler(Consumer<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
	}


	public class WorkerThread extends Thread {

		private boolean executing = false;

		public WorkerThread() {
			int counter = threadCounter.containsKey(EventQueueExecutor.this.threadName) ? threadCounter.get(EventQueueExecutor.this.threadName) : 0;
			super.setName(EventQueueExecutor.this.threadName + "-" + (counter++));
			threadCounter.put(EventQueueExecutor.this.threadName, counter);
			super.setDaemon(EventQueueExecutor.this.daemon);
		}

		@Override
		public void run() {
			while(EventQueueExecutor.this.running || EventQueueExecutor.this.isTaskQueued()){
				EventQueueExecutor.this.waitForTask();
				EventQueueExecutor.this.workingThreads++;
				this.executing = true;
				try{
					EventQueueExecutor.this.execute();
				}catch(Exception e){
					if(EventQueueExecutor.this.errorHandler != null)
						EventQueueExecutor.this.errorHandler.accept(e);
					else
						throw e;
				}
				this.executing = false;
				EventQueueExecutor.this.workingThreads--;
			}
		}
	}
}
