/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.omegazero.common.event.task.LambdaTask;
import org.omegazero.common.event.task.ReflectTask;
import org.omegazero.common.event.task.RunnableTask;
import org.omegazero.common.event.task.Task;

/**
 * Used for running {@link Task}s concurrently, backed by a {@link Queue}, which need not be thread-safe. Up to a predefined number of threads can be used for executing tasks.
 * These parameters may be configured in the constructor or using the {@link Builder}.
 * <p>
 * Tasks are queued using the {@link #queue(Task)} methods. Upon queuing a task, any applicable idle worker thread may pick up the task and {@linkplain Task#run() execute} it.
 * Generally, if no {@link Handle} is used, no guarantee is made about the order in which tasks with the same priority will be executed, as this is also dependent on the backing
 * queue. However, if the backing queue guarantees insertion order for equal priority tasks, and a single worker thread is used, these tasks will be processed in the order in which
 * they were queued.
 * <p>
 * This class is thread-safe.
 * 
 * @since 2.6
 */
public class TaskQueueExecutor {

	private static java.util.Map<String, Integer> threadCounter = new java.util.HashMap<>();


	private final Queue<Task> queue;
	private final int maxWorkerThreadCount;
	private final boolean daemon;
	private final String threadName;

	private final ReentrantLock lock;
	private final Condition taskWaitCondition;

	private final List<WorkerThread> workerThreads = new java.util.ArrayList<WorkerThread>();
	private final AtomicInteger workingThreads = new AtomicInteger();

	private Consumer<Throwable> errorHandler;

	private volatile boolean running = true;

	private long totalTasksExecuted = 0;

	/**
	 * Creates a new {@link TaskQueueExecutor}.
	 * 
	 * @param queue The backing queue
	 * @param maxWorkerThreadCount The maximum number of worker threads allowed
	 * @param daemon Whether worker threads should be daemon threads
	 * @param threadName The thread name prefix of the worker threads
	 * @see Builder
	 */
	public TaskQueueExecutor(Queue<Task> queue, int maxWorkerThreadCount, boolean daemon, String threadName) {
		if(maxWorkerThreadCount <= 0)
			throw new IllegalArgumentException("maxWorkerThreadCount = " + maxWorkerThreadCount);
		this.queue = Objects.requireNonNull(queue);
		this.maxWorkerThreadCount = maxWorkerThreadCount;
		this.daemon = daemon;
		this.threadName = Objects.requireNonNull(threadName);

		this.lock = new ReentrantLock();
		this.taskWaitCondition = this.lock.newCondition();
	}


	/**
	 * Queues a task to be executed by any available worker thread.
	 * <p>
	 * 
	 * Equivalent to a call to:
	 * 
	 * <pre>
	 * {@link #queue(Task, Handle) queue}(task, null)
	 * </pre>
	 * 
	 * @param task The task to queue
	 * @return <code>true</code> if the task was successfully queued
	 * @see #unqueue(Task)
	 */
	public boolean queue(Task task) {
		return this.queue(task, null);
	}

	/**
	 * Queues a task to be executed by an available worker thread. If the backing queue supports prioritization, tasks with the lowest {@linkplain Task#getPriority() priority
	 * value} will be executed first.
	 * <p>
	 * This call may fail and return <code>false</code> if the task could not be added because the queue has reached its maximum size.
	 * <p>
	 * If no worker thread is available or there is a backlog of tasks, and the number of worker threads is below the maximum, a new worker thread will be created.
	 * <p>
	 * An optional {@link Handle} may be given to control task execution. It is guaranteed that no more than one task with the same {@code Handle} will be executed concurrently
	 * (however, note that it is still undefined by which thread each task is executed). Additionally, if the backing queue guarantees insertion order for tasks with the same
	 * priority, it is also guaranteed that all tasks with the same priority and {@code Handle} will be executed in the order they were added.
	 * 
	 * @param task The task to queue
	 * @param handle An optional {@code Handle} to control task execution
	 * @return <code>true</code> if the task was successfully queued
	 * @since 2.9
	 * @see #queue(Task)
	 * @see #unqueue(Task)
	 * @see #newHandle()
	 * @see Queue#offer(Object)
	 */
	public boolean queue(Task task, Handle handle) {
		if(task == null)
			throw new NullPointerException();
		if(!this.running)
			throw new IllegalStateException("Not running");
		if(this.workerThreads.size() < this.maxWorkerThreadCount && (!this.queue.isEmpty() || this.workingThreads.get() >= this.workerThreads.size())){
			this.newWorkerThread();
		}
		boolean res;
		this.lock.lock();
		try{
			if(handle != null)
				res = this.queue.offer(new DelegatingHandleTask(task, handle));
			else
				res = this.queue.offer(task);
			if(res)
				this.totalTasksExecuted++;
			this.taskWaitCondition.signal();
		}finally{
			this.lock.unlock();
		}
		return res;
	}

	/**
	 * Creates a new {@link ReflectTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 * 
	 * @param method The task handler method
	 * @param callerInstance The instance to call the method with. May be <code>null</code> if the method is static
	 * @param args The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see ReflectTask#ReflectTask(Method, Object, Object[], int)
	 */
	public boolean queue(java.lang.reflect.Method method, Object callerInstance, int priority, Object... args) {
		if(method == null || callerInstance == null)
			throw new NullPointerException();
		return this.queue(new ReflectTask(method, callerInstance, args, priority));
	}

	/**
	 * Creates a new {@link RunnableTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 * 
	 * @param handler The task handler
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @since 2.9
	 * @see RunnableTask#RunnableTask(Runnable, int)
	 */
	public boolean queue(Runnable handler, int priority) {
		if(handler == null)
			throw new NullPointerException();
		return this.queue(new RunnableTask(handler, priority));
	}

	/**
	 * Creates a new {@link LambdaTask} instance and adds it to the event queue using {@link #queue(Task)}.
	 * 
	 * @param handler The task handler
	 * @param args The arguments to pass to the task handler when this task is executed
	 * @param priority The priority of this task. May be ignored if the backing queue does not support prioritization
	 * @return <code>true</code> if the task was successfully queued
	 * @see LambdaTask#LambdaTask(Consumer, Object[], int)
	 */
	public boolean queue(Consumer<Object[]> handler, int priority, Object... args) {
		if(handler == null)
			throw new NullPointerException();
		return this.queue(new LambdaTask(handler, args, priority));
	}

	/**
	 * Removes the given task from the queue.
	 * 
	 * @param task The task to remove
	 * @return <code>true</code> if the task was queued and successfully removed
	 * @see #queue(Task)
	 * @see Queue#remove(Object)
	 */
	public boolean unqueue(Task task) {
		if(!this.running)
			throw new IllegalStateException("Not running");
		this.lock.lock();
		try{
			boolean res = this.queue.remove(task);
			if(res)
				this.totalTasksExecuted--;
			return res;
		}finally{
			this.lock.unlock();
		}
	}


	/**
	 * Creates a new {@link Handle} for use in {@link #queue(Task, Handle)}.
	 * <p>
	 * If multiple tasks are queued with the same {@code Handle} instance, it is guaranteed that no more than one task will be executed concurrently (however, note that it is still
	 * undefined by which thread each task is executed). Additionally, if the backing queue guarantees insertion order for tasks with the same priority, it is also guaranteed that
	 * all tasks with the same priority will be executed in the order they were added.
	 * <p>
	 * {@code Handle}s need not be explicitly closed and can simply be discarded (left unreferenced) if no longer used.
	 * 
	 * @return The new {@code Handle}
	 * @since 2.9
	 */
	public Handle newHandle() {
		if(!this.running)
			throw new IllegalStateException("Not running");
		double lowestP = 2;
		WorkerThread lowestLoadedThread = null;
		if(this.totalTasksExecuted > 0){
			for(WorkerThread wt : this.workerThreads){
				double p = (double) wt.executedTasks / this.totalTasksExecuted;
				if(p < lowestP){
					lowestP = p;
					lowestLoadedThread = wt;
				}
			}
		}
		if(lowestLoadedThread == null){
			if(this.workerThreads.size() < this.maxWorkerThreadCount){
				lowestLoadedThread = this.newWorkerThread();
			}else{
				lowestLoadedThread = this.workerThreads.get((int) (Math.random() * this.workerThreads.size()));
			}
		}
		return new Handle(lowestLoadedThread);
	}


	/**
	 * Returns <code>true</code> if the queue is empty.
	 * 
	 * @return <code>true</code> if the queue is empty
	 */
	public boolean isQueueEmpty() {
		this.lock.lock();
		try{
			return this.queue.isEmpty();
		}finally{
			this.lock.unlock();
		}
	}

	/**
	 * Returns the number of queued tasks.
	 * 
	 * @return Number of queued tasks
	 */
	public int getQueuedTaskCount() {
		this.lock.lock();
		int totalLocal = 0;
		for(WorkerThread wt : this.workerThreads)
			totalLocal += wt.localQueue.size();
		try{
			return totalLocal + this.queue.size();
		}finally{
			this.lock.unlock();
		}
	}


	/**
	 * Shuts this {@link TaskQueueExecutor} down by gracefully stopping the worker threads.
	 * <p>
	 * Equivalent to a call to:
	 * <pre><code>
	 * {@link #exit(boolean) exit}(false)
	 * </code></pre>
	 */
	public void exit(){
		this.exit(false);
	}

	/**
	 * Shuts this {@link TaskQueueExecutor} down by interrupting all idle worker threads, causing them to exit. Worker threads that are currently running a task are not
	 * interrupted, but will exit after finishing the task.
	 * <p>
	 * If <b>blocking</b> is <code>true</code>, the calling thread will be blocked until all worker threads have exited. If the calling thread is interrupted while waiting, this
	 * method returns <code>false</code>.
	 * 
	 * @param blocking {@code true} to wait for all worker threads to exit
	 * @return <code>true</code> if the calling thread was interrupted while waiting for the worker threads to exit
	 */
	public boolean exit(boolean blocking){
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
	 * @see #getMaxWorkerThreadCount()
	 */
	public int getWorkerThreadCount() {
		return this.workerThreads.size();
	}

	/**
	 * Sets the error handler that will be called when an error occurs while executing a task in any of the worker threads.
	 * <p>
	 * If this handler is not set, the error will be {@linkplain Throwable#printStackTrace() printed to <code>stderr</code>}.
	 * 
	 * @param errorHandler The error handler, or {@code null} to remove an existing error handler
	 */
	public void setErrorHandler(Consumer<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
	}


	private WorkerThread newWorkerThread() {
		if(!this.running)
			throw new IllegalStateException("Not running");
		if(this.workerThreads.size() >= this.maxWorkerThreadCount)
			throw new IllegalStateException("Max worker thread count reached");
		WorkerThread n = new WorkerThread();
		this.workerThreads.add(n);
		return n;
	}


	/**
	 * Builder used to create a {@link TaskQueueExecutor}.
	 * <p>
	 * A <code>TaskQueueExecutor</code> backed by any queue may be created using {@link TaskQueueExecutor#from(Queue)}, or with any of the predefined backing queues using
	 * {@link TaskQueueExecutor#fromPriority()} and {@link TaskQueueExecutor#fromSequential()}.
	 * 
	 * @since 2.6
	 */
	public static class Builder {

		private final Queue<Task> queue;
		private int maxWorkerThreadCount = 1;
		private boolean daemon = false;
		private String threadName = "TaskExecutor";

		private Builder(Queue<Task> queue) {
			this.queue = Objects.requireNonNull(queue);
		}


		/**
		 * Sets the maximum number of worker threads allowed for the {@link TaskQueueExecutor}. This method may be called with a negative value to set the maximum number to
		 * its absolute value or the {@linkplain Runtime#availableProcessors() number of processors available}, whichever is greater.
		 * For example, passing {@code -2} sets the maximum number of worker threads to {@code 2}, or the number of processors if there are more than 2.
		 * <p>
		 * The default value is <code>1</code>.
		 * 
		 * @param maxWorkerThreadCount The maximum number of worker threads
		 * @return This builder
		 */
		public Builder workerThreads(int maxWorkerThreadCount) {
			if(maxWorkerThreadCount < 0)
				this.maxWorkerThreadCount = Math.max(Runtime.getRuntime().availableProcessors(), -maxWorkerThreadCount);
			else
				this.maxWorkerThreadCount = maxWorkerThreadCount;
			return this;
		}

		/**
		 * Sets whether worker threads of the {@link TaskQueueExecutor} should be daemon threads. Setting this to <code>true</code> will not require an explicit call to
		 * {@link TaskQueueExecutor#exit(boolean)} for the VM to exit.
		 * <p>
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
		 * Sets worker thread name prefix for the {@link TaskQueueExecutor}.
		 * <p>
		 * The default value is <code>"TaskExecutor"</code>.
		 * 
		 * @param threadName The thread name prefix
		 * @return This builder
		 */
		public Builder name(String threadName) {
			this.threadName = Objects.requireNonNull(threadName);
			return this;
		}


		/**
		 * Builds a {@link TaskQueueExecutor} with previously set parameters.
		 * 
		 * @return The new <code>TaskQueueExecutor</code>
		 * @see TaskQueueExecutor#TaskQueueExecutor(Queue, int, boolean, String)
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
	public static Builder from(Queue<Task> queue) {
		return new Builder(queue);
	}

	/**
	 * Creates a new {@link Builder} for a {@link TaskQueueExecutor} backed by a {@link java.util.PriorityQueue}. This queue supports prioritization, but makes no guarantee about
	 * the order at which tasks with the same priority are executed.
	 * 
	 * @return The builder
	 * @see #from(Queue)
	 * @see #fromSequential()
	 */
	public static Builder fromPriority() {
		return new Builder(new java.util.PriorityQueue<>());
	}

	/**
	 * Creates a new {@link Builder} for a {@link TaskQueueExecutor} backed by a {@link java.util.ArrayDeque}. This queue does not support prioritization, but has insertion order,
	 * meaning tasks will generally be executed in the order they were queued.
	 * 
	 * @return The builder
	 * @see #from(Queue)
	 * @see #fromPriority()
	 */
	public static Builder fromSequential() {
		return new Builder(new java.util.ArrayDeque<>());
	}


	/**
	 * An opaque class used for task execution control. See {@link TaskQueueExecutor#newHandle()}.
	 * 
	 * @since 2.9
	 */
	public class Handle {


		private final WorkerThread wt;

		private Handle(WorkerThread wt) {
			this.wt = wt;
		}


		/**
		 * Queues the given {@code Task} with this handle.
		 * <p>
		 * 
		 * Equivalent to a call to:
		 * 
		 * <pre>
		 * {@link TaskQueueExecutor#queue(Task, Handle) queue}(task, this)
		 * </pre>
		 * 
		 * @param task The task to queue
		 * @return <code>true</code> if the task was successfully queued
		 */
		public boolean queue(Task task) {
			return TaskQueueExecutor.this.queue(task, this);
		}

		/**
		 * Equivalent to {@link TaskQueueExecutor#queue(Method, Object, int, Object...)}, but queued with this handle.
		 * 
		 * @param method The task handler method
		 * @param callerInstance The instance to call the method with
		 * @param args The arguments to pass to the task handler when this task is executed
		 * @param priority The priority of this task
		 * @return <code>true</code> if the task was successfully queued
		 */
		public boolean queue(java.lang.reflect.Method method, Object callerInstance, int priority, Object... args) {
			return this.queue(new ReflectTask(method, callerInstance, args, priority));
		}

		/**
		 * Equivalent to {@link TaskQueueExecutor#queue(Runnable, int)}, but queued with this handle.
		 * 
		 * @param handler The task handler
		 * @param priority The priority of this task
		 * @return <code>true</code> if the task was successfully queued
		 */
		public boolean queue(Runnable handler, int priority) {
			return this.queue(new RunnableTask(handler, priority));
		}

		/**
		 * Equivalent to {@link TaskQueueExecutor#queue(Consumer, int, Object...)}, but queued with this handle.
		 * 
		 * @param handler The task handler
		 * @param args The arguments to pass to the task handler when this task is executed
		 * @param priority The priority of this task
		 * @return <code>true</code> if the task was successfully queued
		 */
		public boolean queue(Consumer<Object[]> handler, int priority, Object... args) {
			return this.queue(new LambdaTask(handler, args, priority));
		}
	}

	private class WorkerThread extends Thread {


		private final Queue<Task> localQueue = new java.util.concurrent.ConcurrentLinkedDeque<>();

		private volatile boolean executing;
		private volatile boolean waiting;

		private long executedTasks = 0;

		public WorkerThread() {
			int counter;
			synchronized(threadCounter){
				Integer temp = threadCounter.get(TaskQueueExecutor.this.threadName);
				counter = temp != null ? temp : 0;
				threadCounter.put(TaskQueueExecutor.this.threadName, counter + 1);
			}
			super.setName(TaskQueueExecutor.this.threadName + "-" + counter);
			super.setDaemon(TaskQueueExecutor.this.daemon);
			super.start();
		}


		private Task nextTask() throws InterruptedException {
			TaskQueueExecutor.this.lock.lockInterruptibly();
			try{
				while(true){ // for staying in this method if a task is a DelegatingHandleTask for another thread
					Task task;
					if((task = this.localQueue.poll()) != null)
						return task;
					while((task = TaskQueueExecutor.this.queue.poll()) == null){
						boolean interrupted = false;
						this.waiting = true;
						try{
							TaskQueueExecutor.this.taskWaitCondition.await();
							interrupted = Thread.interrupted();
						}catch(InterruptedException e){
							interrupted = true;
						}finally{
							this.waiting = false;
						}
						if(!TaskQueueExecutor.this.running)
							throw new InterruptedException("exiting");
						else if((task = this.localQueue.poll()) != null)
							return task;
						else if(interrupted)
							throw new AssertionError("WorkerThread interrupted unexpectedly");
					}
					if(task instanceof DelegatingHandleTask){
						DelegatingHandleTask htask = (DelegatingHandleTask) task;
						WorkerThread wt = htask.handle.wt;
						if(wt != this){
							wt.localQueue.add(task);
							if(wt.waiting)
								wt.interrupt();
							continue;
						}
					}
					return task;
				}
			}finally{
				TaskQueueExecutor.this.lock.unlock();
			}
		}

		private void runTask(Task task){
			TaskQueueExecutor.this.workingThreads.incrementAndGet();
			this.executing = true;
			try{
				task.run();
			}catch(Exception e){
				if(TaskQueueExecutor.this.errorHandler != null)
					TaskQueueExecutor.this.errorHandler.accept(e);
				else
					e.printStackTrace();
			}finally{
				Thread.interrupted(); // clear interrupt status
				this.executedTasks++;
				this.executing = false;
				TaskQueueExecutor.this.workingThreads.decrementAndGet();
			}
		}

		private void executeRemainingTasks(){
			TaskQueueExecutor.this.lock.lock();
			try{
				Task task;
				while((task = TaskQueueExecutor.this.queue.poll()) != null){
					TaskQueueExecutor.this.lock.unlock();
					this.runTask(task);
					TaskQueueExecutor.this.lock.lock();
				}
				while((task = this.localQueue.poll()) != null)
					this.runTask(task);
			}finally{
				TaskQueueExecutor.this.lock.unlock();
			}
		}


		@Override
		public void run() {
			while(TaskQueueExecutor.this.running){
				Task task;
				try{
					task = this.nextTask();
				}catch(InterruptedException e){
					break;
				}
				this.runTask(task);
			}
			this.executeRemainingTasks();
		}
	}


	private static class DelegatingHandleTask implements Task {

		private final Task delegate;
		private final Handle handle;

		public DelegatingHandleTask(Task delegate, Handle handle) {
			this.delegate = delegate;
			this.handle = handle;
		}


		@Override
		public void run() {
			this.delegate.run();
		}

		@Override
		public int getPriority() {
			return this.delegate.getPriority();
		}
	}
}
