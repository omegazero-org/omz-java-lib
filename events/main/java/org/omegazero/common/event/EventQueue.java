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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.omegazero.common.event.task.LambdaTask;
import org.omegazero.common.event.task.ReflectTask;
import org.omegazero.common.event.task.Task;

/**
 * Used for queuing {@link Task}s.
 * 
 * @deprecated Since 2.6. Use {@link java.util.concurrent.BlockingQueue} and its implementing classes instead.
 */
@Deprecated
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
			this.tasks.ensureCapacity(maxTasks);
	}


	/**
	 * Queues a task to be executed in a subsequent {@link EventQueue#execute()} call. Tasks with the highest value for {@link Task#priority} will be executed first.<br>
	 * <br>
	 * A thread waiting on this queue's monitor will be notified.
	 * 
	 * @param task The task to queue
	 */
	public synchronized void queue(Task task) {
		if(this.tasks.size() + 1 > this.maxTasks)
			throw new RuntimeException("Event Queue task limit exceeded: " + this.tasks.size());
		int index = this.tasks.size();
		for(int i = 0; i < this.tasks.size(); i++){
			if(this.tasks.get(i).getPriority() > task.getPriority()){
				index = i;
				break;
			}
		}
		this.tasks.add(index, task);
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
		return this.tasks.remove(this.tasks.size() - 1);
	}

	/**
	 * Executes the {@link Task} with the highest priority. If no task is available, this is a no-op.
	 */
	public void execute() {
		Task t = this.getNextTask();
		if(t == null)
			return;
		try{
			t.run();
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
		this.tasks.clear();
	}

	/**
	 * 
	 * @return <b>true</b> if there is at least one task queued.
	 */
	public boolean isTaskQueued() {
		return this.tasks.size() > 0;
	}

	/**
	 * 
	 * @return The number of queued tasks.
	 */
	public int getQueuedTaskCount() {
		return this.tasks.size();
	}
}
