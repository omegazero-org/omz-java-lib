/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.omegazero.common.event.task.LambdaTask;
import org.omegazero.common.event.task.ReflectTask;
import org.omegazero.common.event.task.Task;

/**
 * Used for running {@link Task}s concurrently, backed by a {@link BlockingQueue}. Up to a predefined number of threads may be used for executing tasks. These parameters may
 * be configured in the constructor or using the {@link Builder}.<br>
 * <br>
 * Tasks are queued using the {@link #queue(Task)} methods. Upon queuing a task, any idle worker thread may pick up the task and {@linkplain Task#run() execute} it. Generally,
 * no guarantee is made about the order in which tasks with the same priority will be executed, as this is also dependent on the backing queue. However, if the backing queue
 * guarantees insertion order for equal priority tasks, and a single worker thread is used, these tasks will be processed in the order in which they were queued.
 * 
 * @since 2.6
 */
public class TaskQueueExecutor {

	private static java.util.Map<String, Integer> threadCounter = new java.util.concurrent.ConcurrentHashMap<>();


	private final BlockingQueue<Task> queue;
	private final int maxWorkerThreadCount;
	private final boolean daemon;
	private final String threadName;

	private List<WorkerThread> workerThreads = new java.util.ArrayList<WorkerThread>();
	private AtomicInteger workingThreads = new AtomicInteger();

	private Consumer<Throwable> errorHandler;

	private volatile boolean running = true;

	/**
	 * Creates a new {@link TaskQueueExecutor}.
	 * 
	 * @param queue                The backing queue
	 * @param maxWorkerThreadCount The maximum number of worker threads allowed
	 * @param daemon               Whether worker threads should be daemon threads
	 * @param threadName           The thread name prefix of the worker threads
	 * @see Builder
	 */
	public TaskQueueExecutor(BlockingQueue<Task> queue, int maxWorkerThreadCount, boolean daemon, String threadName) {
		if(queue == null || threadName == null)
			throw new NullPointerException();
		if(maxWorkerThreadCount <= 0)
			throw new IllegalArgumentException("maxWorkerThreadCount = " + maxWorkerThreadCount);
		this.queue = queue;
		this.maxWorkerThreadCount = maxWorkerThreadCount;
		this.daemon = daemon;
		this.threadName = threadName;
	}


	/**
	 * Queues a task to be executed by an available worker thread. If the backing queue supports prioritization, tasks with the lowest {@linkplain Task#getPriority() priority
	 * value} will be executed first.<br>
	 * <br>
	 * This call may fail and return <code>false</code> if the task could not be added because the queue has reached its maximum size.<br>
	 * <br>
	 * If no worker thread is available or there is a backlog of tasks, and the number of worker threads is below the maximum, a new worker thread will be created.
	 * 
	 * @param task The task to queue
	 * @return <code>true</code> if the task was successfully queued
	 * @see #unqueue(Task)
	 * @see BlockingQueue#offer(Object)
	 */
	public boolean queue(Task task) {
		if(this.workerThreads.size() < this.maxWorkerThreadCount && (!this.queue.isEmpty() || this.workingThreads.get() >= this.workerThreads.size())){
			this.workerThreads.add(new WorkerThread());
		}
		return this.queue.offer(task);
	}

	/**
	 * Creates a new {@link ReflectTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 * 
	 * @param method         The task handler method
	 * @param callerInstance The instance to call the method with. May be <code>null</code> if the method is static
	 * @param args           The arguments to pass to the task handler when this task is executed
	 * @param priority       The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see #queue(Task)
	 * @see ReflectTask#ReflectTask(Method, Object, Object[], int)
	 */
	public boolean queue(java.lang.reflect.Method method, Object callerInstance, int priority, Object... args) {
		return this.queue(new ReflectTask(method, callerInstance, args, priority));
	}

	/**
	 * Creates a new {@link LambdaTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 * 
	 * @param handler  The task handler
	 * @param args     The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see #queue(Task)
	 * @see LambdaTask#LambdaTask(Consumer, Object[], int)
	 */
	public boolean queue(Consumer<Object[]> handler, int priority, Object... args) {
		return this.queue(new LambdaTask(handler, args, priority));
	}

	/**
	 * Removes the given task from the queue.
	 * 
	 * @param task The task to remove
	 * @return <code>true</code> if the task was queued and successfully removed
	 * @see #queue(Task)
	 * @see BlockingQueue#remove(Object)
	 */
	public boolean unqueue(Task task) {
		return this.queue.remove(task);
	}

	/**
	 * Returns <code>true</code> if the queue is empty.
	 * 
	 * @return <code>true</code> if the queue is empty
	 */
	public boolean isQueueEmpty() {
		return this.queue.isEmpty();
	}

	/**
	 * Returns the number of queued tasks.
	 * 
	 * @return Number of queued tasks
	 */
	public int getQueuedTaskCount() {
		return this.queue.size();
	}


	/**
	 * Shuts this {@link TaskQueueExecutor} down by gracefully stopping the worker threads.<br>
	 * <br>
	 * 
	 * Equivalent to a call to:
	 * 
	 * <pre>
	 * {@link #exit(boolean) exit}(false)
	 * </pre>
	 */
	public void exit() {
		this.exit(false);
	}

	/**
	 * Shuts this {@link TaskQueueExecutor} down by interrupting all idle worker threads, causing them to exit. Worker threads that are currently running a task are not
	 * interrupted, but will exit after finishing the task.<br>
	 * <br>
	 * If <b>blocking</b> is <code>true</code>, the calling thread will be blocked until all worker threads have exited. If the calling thread is interrupted while waiting,
	 * this method returns <code>false</code>.
	 * 
	 * @param blocking Whether to wait for all worker threads to exit
	 * @return <code>true</code> if the calling thread was interrupted while waiting for the worker threads to exit
	 */
	public boolean exit(boolean blocking) {
		synchronized(this){
			if(!this.running)
				throw new IllegalStateException("Not running");
			this.running = false;
			for(WorkerThread thread : this.workerThreads){
				if(!thread.executing)
					thread.interrupt();
			}
		}
		if(blocking){
			for(WorkerThread thread : this.workerThreads){
				try{
					thread.join();
				}catch(InterruptedException e){
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * Returns the configured maximum number of worker threads.
	 * 
	 * @return The maximum number of worker threads
	 */
	public int getMaxWorkerThreadCount() {
		return this.maxWorkerThreadCount;
	}

	/**
	 * Returns the current number of worker threads.
	 * 
	 * @return The number of active worker threads
	 * @see #getMaxThreads()
	 */
	public int getWorkerThreadCount() {
		return this.workerThreads.size();
	}

	/**
	 * Sets the error handler that will be called when an error occurs while executing a task in any of the worker threads.<br>
	 * <br>
	 * If this handler is not set, the error will be {@linkplain Throwable#printStackTrace() printed to <code>stderr</code>}.
	 * 
	 * @param errorHandler The error handler
	 */
	public void setErrorHandler(Consumer<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
	}


	/**
	 * Builder used to create a {@link TaskQueueExecutor}.<br>
	 * <br>
	 * A <code>TaskQueueExecutor</code> backed by any queue may be created using {@link TaskQueueExecutor#from(BlockingQueue)}, or with any of the predefined backing queues
	 * using {@link TaskQueueExecutor#fromPriority()} and {@link TaskQueueExecutor#fromSequential()}.
	 * 
	 * @since 2.6
	 */
	public static class Builder {

		private final BlockingQueue<Task> queue;
		private int maxWorkerThreadCount = 1;
		private boolean daemon = false;
		private String threadName = "TaskExecutor";

		private Builder(BlockingQueue<Task> queue) {
			if(queue == null)
				throw new NullPointerException();
			this.queue = queue;
		}


		/**
		 * Sets the maximum number of worker threads allowed for the {@link TaskQueueExecutor}. This method may be called with a value of <code>-1</code> to set the maximum
		 * number to the number of processors available.<br>
		 * <br>
		 * The default value is <code>1</code>.
		 * 
		 * @param maxWorkerThreadCount The maximum number of worker threads
		 * @return This builder
		 */
		public Builder workerThreads(int maxWorkerThreadCount) {
			if(maxWorkerThreadCount < 0)
				this.maxWorkerThreadCount = Runtime.getRuntime().availableProcessors();
			else
				this.maxWorkerThreadCount = maxWorkerThreadCount;
			return this;
		}

		/**
		 * Sets whether worker threads of the {@link TaskQueueExecutor} should be daemon threads. Setting this to <code>true</code> will not require an explicit call to
		 * {@link TaskQueueExecutor#exit(boolean)} for the VM to exit.<br>
		 * <br>
		 * The default value is <code>false</code>.
		 * 
		 * @param daemon Whether worker threads should be daemon threads
		 * @return This builder
		 */
		public Builder daemon(boolean daemon) {
			this.daemon = daemon;
			return this;
		}

		/**
		 * Sets worker thread name prefix for the {@link TaskQueueExecutor}.<br>
		 * <br>
		 * The default value is <code>"TaskExecutor"</code>.
		 * 
		 * @param threadName The thread name prefix
		 * @return This builder
		 */
		public Builder name(String threadName) {
			if(threadName == null)
				throw new NullPointerException();
			this.threadName = threadName;
			return this;
		}


		/**
		 * Builds a {@link TaskQueueExecutor} with previously set parameters.
		 * 
		 * @return The new <code>TaskQueueExecutor</code>
		 * @see TaskQueueExecutor#TaskQueueExecutor(BlockingQueue, int, boolean, String)
		 */
		public TaskQueueExecutor build() {
			return new TaskQueueExecutor(this.queue, this.maxWorkerThreadCount, this.daemon, this.threadName);
		}
	}


	/**
	 * Creates a new {@link Builder} with the given backing <b>queue</b> for the {@link TaskQueueExecutor}.
	 * 
	 * @param queue The backing queue
	 * @return The builder
	 * @see #fromPriority()
	 * @see #fromSequential()
	 */
	public static Builder from(BlockingQueue<Task> queue) {
		return new Builder(queue);
	}

	/**
	 * Creates a new {@link Builder} for a {@link TaskQueueExecutor} backed by a {@link PriorityBlockingQueue}. This queue supports prioritization, but makes no guarantee
	 * about the order at which tasks are executed.
	 * 
	 * @return The builder
	 * @see #from(BlockingQueue)
	 * @see #fromSequential()
	 */
	public static Builder fromPriority() {
		return new Builder(new PriorityBlockingQueue<>());
	}

	/**
	 * Creates a new {@link Builder} for a {@link TaskQueueExecutor} backed by a {@link LinkedBlockingQueue}. This queue does not support prioritization, but has insertion
	 * order, meaning tasks will generally be executed in the order they were queued.
	 * 
	 * @return The builder
	 * @see #from(BlockingQueue)
	 * @see #fromPriority()
	 */
	public static Builder fromSequential() {
		return new Builder(new LinkedBlockingQueue<>());
	}


	private class WorkerThread extends Thread {


		private volatile boolean executing;

		public WorkerThread() {
			Integer temp = threadCounter.get(TaskQueueExecutor.this.threadName);
			int counter = temp != null ? temp : 0;
			threadCounter.put(TaskQueueExecutor.this.threadName, counter + 1);
			super.setName(TaskQueueExecutor.this.threadName + "-" + counter);
			super.setDaemon(TaskQueueExecutor.this.daemon);
			super.start();
		}


		@Override
		public void run() {
			while(TaskQueueExecutor.this.running){
				Task task;
				try{
					task = TaskQueueExecutor.this.queue.take();
				}catch(InterruptedException e){
					break;
				}
				TaskQueueExecutor.this.workingThreads.incrementAndGet();
				this.executing = true;
				try{
					task.run();
				}catch(Exception e){
					if(TaskQueueExecutor.this.errorHandler != null)
						TaskQueueExecutor.this.errorHandler.accept(e);
					else
						e.printStackTrace();
				}
				this.executing = false;
				TaskQueueExecutor.this.workingThreads.decrementAndGet();
			}
		}
	}
}
