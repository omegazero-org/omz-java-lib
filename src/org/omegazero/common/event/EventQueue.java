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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EventQueue {

	private final int maxTasks;

	private final ArrayList<Task> tasks = new ArrayList<>();


	/**
	 * Creates a new event queue.<br>
	 * <br>
	 * The default values are:<br>
	 * <b>maxTasks</b>: 65536<br>
	 * <b>preallocate</b>: false
	 * 
	 * @see EventQueue#EventQueue(int, boolean)
	 */
	public EventQueue() {
		this(65536, false);
	}

	/**
	 * Creates a new event queue.
	 * 
	 * @param maxTasks    The maximum number of tasks that may be queued at once
	 * @param preallocate If <b>true</b>, the list storing the tasks will initially reserve enough space to store <b>maxTasks</b>, saving overhead when queuing tasks later
	 */
	public EventQueue(int maxTasks, boolean preallocate) {
		this.maxTasks = maxTasks;
		if(preallocate)
			tasks.ensureCapacity(maxTasks);
	}


	/**
	 * Queues a task to be executed in a subsequent {@link EventQueue#execute()} call. Tasks with the highest value for {@link Task#priority} will be executed first.<br>
	 * <br>
	 * A thread waiting on this queue's monitor will be notified.
	 * 
	 * @param task The task to queue
	 */
	public synchronized void queue(Task task) {
		if(tasks.size() + 1 > this.maxTasks)
			throw new RuntimeException("Event Queue task limit exceeded: " + tasks.size());
		int index = tasks.size();
		for(int i = 0; i < tasks.size(); i++){
			if(tasks.get(i).getPriority() > task.getPriority()){
				index = i;
				break;
			}
		}
		tasks.add(index, task);
		this.notify();
	}

	/**
	 * Creates a new {@link ReflectTask} instance and adds it to the event queue using {@link EventQueue#queue(Task)}.
	 * 
	 * @see EventQueue#queue(Task)
	 * @see ReflectTask#ReflectTask(Method, Object, Object[], int)
	 */
	public void queue(Method method, Object callerInstance, int priority, Object... args) {
		this.queue(new ReflectTask(method, callerInstance, args, priority));
	}

	/**
	 * Creates a new {@link LambdaTask} instance and adds it to the event queue using {@link EventQueue#queue(Task)}.
	 * 
	 * @see EventQueue#queue(Task)
	 * @see LambdaTask#LambdaTask(Consumer, Object[], int)
	 */
	public void queue(Consumer<Object[]> handler, int priority, Object... args) {
		this.queue(new LambdaTask(handler, args, priority));
	}


	protected synchronized Task getNextTask() {
		if(!this.isTaskQueued())
			return null;
		return tasks.remove(tasks.size() - 1);
	}

	/**
	 * Executes the {@link Task} with the highest priority. If no task is available, this is a no-op.
	 */
	public void execute() {
		Task t = this.getNextTask();
		if(t == null)
			return;
		try{
			t.execute();
		}catch(Throwable e){
			throw new RuntimeException("Error executing task", e);
		}
	}

	/**
	 * Blocks the thread until a task to execute becomes available, then executes it.
	 */
	public void executeBlocking() {
		this.waitForTask();
		this.execute();
	}

	/**
	 * Blocks the thread until a task to execute becomes available.
	 */
	public synchronized void waitForTask() {
		while(!this.isTaskQueued())
			try{
				this.wait();
			}catch(InterruptedException e){
				return;
			}
	}


	/**
	 * Removes all queued tasks from the event queue.
	 */
	public void clearTasks() {
		tasks.clear();
	}

	/**
	 * 
	 * @return <b>true</b> if there is at least one task queued.
	 */
	public boolean isTaskQueued() {
		return tasks.size() > 0;
	}

	/**
	 * 
	 * @return The number of queued tasks.
	 */
	public int getQueuedTaskCount() {
		return tasks.size();
	}


	public static abstract class Task {

		private final int priority;
		private Object[] args;

		public Task(int priority, Object[] args) {
			this.priority = priority;
			this.args = args;
		}


		public int getPriority() {
			return priority;
		}

		public Object[] getArgs() {
			return args;
		}

		public abstract void execute() throws Throwable;
	}

	public static class ReflectTask extends Task {

		private Method method;
		private Object callerInstance;

		/**
		 * Constructs a new <b>ReflectTask</b>. ReflectTasks call the handler using Java reflection.
		 * 
		 * @param method         The method to be called when this task gets executed
		 * @param callerInstance The instance the method is called with
		 * @param args           Arguments to be passed to the called method
		 * @param priority       The task priority. Higher priorities will be executed first
		 */
		public ReflectTask(Method method, Object callerInstance, Object[] args, int priority) {
			super(priority, args);
			this.method = method;
			this.callerInstance = callerInstance;
		}


		@Override
		public void execute() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			method.invoke(callerInstance, super.getArgs());
		}

		public Method getMethod() {
			return method;
		}

		public Object getCallerInstance() {
			return callerInstance;
		}
	}

	public static class LambdaTask extends Task {

		private Consumer<Object[]> handler;

		/**
		 * Constructs a new <b>LambdaTask</b>.
		 * 
		 * @param run      The handler to be called when this task gets executed
		 * @param args     Arguments to be passed to the handler
		 * @param priority The task priority. Higher priorities will be executed first
		 */
		public LambdaTask(Consumer<Object[]> handler, Object[] args, int priority) {
			super(priority, args);
			this.handler = handler;
		}


		@Override
		public void execute() {
			handler.accept(super.getArgs());
		}
	}
}
