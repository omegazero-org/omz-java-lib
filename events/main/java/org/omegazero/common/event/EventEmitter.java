/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omegazero.common.event.runnable.GenericRunnable;
import org.omegazero.common.event.task.ExecutionFailedException;

/**
 * Used for distributing and executing events among a set of registered listeners.
 * <p>
 * Listeners are added to a specific event name using {@link #addEventListener(String, GenericRunnable)} or similar methods. Upon dispatching an event using {@link #runEvent(String, Object...)},
 * all event listeners registered for the given event name are called with the given arguments. The number of arguments passed and the number of arguments in the event listeners must match, otherwise
 * an {@code IllegalArgumentException} will be thrown at the listener-level.
 * <p>
 * By default, all event listeners are executed regardless of any exceptions thrown at the listener-level. If any listener did throw an exception, the {@code runEvent} call will throw an
 * {@code ExecutionFailedException} detailing the thrown exception(s), after all listeners were executed. See also: {@link #setCoalesceListenerErrors(boolean)}.
 * <p>
 * A call to {@link #createEventId(String, int)} will switch this {@code EventEmitter} to <i>fastAccess</i> mode, which may be used for more efficient event dispatching. In this mode, all event
 * names must be registered with a call to this method.
 * <p>
 * This class is thread-safe.
 *
 * @since 2.10
 * @apiNote The behavior and API naming of this class is heavily inspired by the similar <a href="https://nodejs.org/api/events.html">Node.js events API</a>
 */
public class EventEmitter {

	private Map<String, Object> events = new java.util.HashMap<>();
	private EventListenersObj[] fastAccessEventHandlers;
	private int fastAccessIdCounter = -1;

	private boolean coalesceListenerErrors = true;


	/**
	 * Creates an ID for the given event name used for more efficient event dispatching.
	 * <p>
	 * Equivalent to a call to:
	 * <pre><code>
	 * 	{@link #createEventId(String, int) createEventId}(name, -1)
	 * </code></pre>
	 *
	 * @param name The event name
	 * @return The {@code EventEmitter}-scope unique event ID
	 */
	public int createEventId(String name){
		return this.createEventId(name, -1);
	}

	/**
	 * Creates an ID for the given event name used for more efficient event dispatching.
	 * <p>
	 * Calling this method switches this {@code EventEmitter} to <i>fastAccess</i> mode.
	 * <p>
	 * If {@code expect} is non-negative and the generated next event ID does not match its value, no event ID will be created and an {@code IllegalStateException} thrown. This may be used by
	 * applications to assert a specific event ID, which removes the requirement for runtime event ID storage.
	 *
	 * @param name The event name
	 * @param expect The expected event ID
	 * @return The {@code EventEmitter}-scope unique event ID, same as {@code expect} if non-negative
	 * @throws IllegalStateException If {@code expect} is non-negative and does not match the next event ID
	 * @throws IllegalStateException If any listeners were registered already
	 */
	public synchronized int createEventId(String name, int expect){
		int id = this.fastAccessIdCounter + 1;
		if(id == 0 && !this.events.isEmpty())
			throw new IllegalStateException("createEventId cannot be called when listeners are active");
		if(expect >= 0 && expect != id)
			throw new IllegalStateException("createEventId: Expectation failed: expect=" + expect + " id=" + id);
		this.events.put(name, id);
		this.reserveEventIdSpace(id);
		this.fastAccessIdCounter++;
		return id;
	}

	/**
	 * Pre-allocates internal data structures for storing event IDs up to <b>highestId</b>.
	 *
	 * @param highestId The highest event ID
	 * @since 2.11.0
	 */
	public synchronized void reserveEventIdSpace(int highestId){
		int num = highestId + 1;
		if(this.fastAccessEventHandlers == null)
			this.fastAccessEventHandlers = new EventListenersObj[num];
		else if(this.fastAccessEventHandlers.length < num)
			this.fastAccessEventHandlers = Arrays.copyOfRange(this.fastAccessEventHandlers, 0, num);
	}


	private EventListenersObj getEventListeners0(String name, boolean create){
		assert Thread.holdsLock(this);
		EventListenersObj listeners;
		if(this.fastAccessIdCounter >= 0){
			Integer idO = (Integer) this.events.get(name);
			if(idO == null){
				if(!create)
					return null;
				throw new IllegalStateException("Cannot create listener list for unregistered event name '" + name + "' (fastAccess is enabled, call createEventId first)");
			}
			listeners = this.fastAccessEventHandlers[idO];
			if(listeners == null){
				if(!create)
					return null;
				listeners = new EventListenersObj();
				this.fastAccessEventHandlers[idO] = listeners;
			}
		}else{
			listeners = (EventListenersObj) this.events.get(name);
			if(listeners == null){
				if(!create)
					return null;
				listeners = new EventListenersObj();
				this.events.put(name, listeners);
			}
		}
		return listeners;
	}


	/**
	 * Alias for {@link #addEventListener(String, GenericRunnable)}.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @return This {@code EventEmitter}
	 */
	public EventEmitter on(String name, GenericRunnable runnable){
		this.addEventListener(name, runnable);
		return this;
	}

	/**
	 * Alias for {@link #addEventListenerOnce(String, GenericRunnable)}.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @return This {@code EventEmitter}
	 */
	public EventEmitter once(String name, GenericRunnable runnable){
		this.addEventListenerOnce(name, runnable);
		return this;
	}

	/**
	 * Alias for {@link #removeEventListener(String, GenericRunnable)}.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @return This {@code EventEmitter}
	 */
	public EventEmitter off(String name, GenericRunnable runnable){
		this.removeEventListener(name, runnable);
		return this;
	}

	/**
	 * Alias for {@link #runEvent(String, Object...)}.
	 *
	 * @param name The event name
	 * @param args The arguments to pass to the event listeners
	 * @return The number of registered listeners
	 */
	public int emit(String name, Object... args){
		return this.runEvent(name, args);
	}

	/**
	 * Alias for {@link #runEvent(int, Object...)}.
	 *
	 * @param id The event ID
	 * @param args The arguments to pass to the event listeners
	 * @return The number of registered listeners
	 */
	public int emit(int id, Object... args){
		return this.runEvent(id, args);
	}


	/**
	 * Registers the given event listener for the given event {@code name}. The last event listener registered with this method will always be run first among the set of registered event listeners.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @see #addEventListener(String, GenericRunnable)
	 */
	public synchronized void prependEventListener(String name, GenericRunnable runnable){
		EventListenersObj listeners = this.getEventListeners0(name, true);
		synchronized(listeners){
			listeners.list.add(0, runnable);
			listeners.listChanged();
		}
	}

	/**
	 * Registers the given event listener for the given event {@code name}.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @throws IllegalStateException If <i>fastAccess</i> is enabled and the given event {@code name} was not created using {@link #createEventId(String, int)}
	 * @see #on(String, GenericRunnable)
	 * @see #prependEventListener(String, GenericRunnable)
	 * @see #addEventListenerOnce(String, GenericRunnable)
	 * @see #removeEventListener(String, GenericRunnable)
	 */
	public synchronized void addEventListener(String name, GenericRunnable runnable){
		EventListenersObj listeners = this.getEventListeners0(name, true);
		synchronized(listeners){
			listeners.list.add(runnable);
			listeners.listChanged();
		}
	}

	/**
	 * Registers the given event listener for the given event {@code name}. If the an event with the given name is executed, this listener will be unregistered before its execution, causing the event
	 * listener to only run once.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @throws IllegalStateException If <i>fastAccess</i> is enabled and the given event {@code name} was not created using {@link #createEventId(String, int)}
	 * @see #addEventListener(String, GenericRunnable)
	 */
	public synchronized void addEventListenerOnce(String name, GenericRunnable runnable){
		new OnceRunnable(name, runnable);
	}

	/**
	 * Removes the given event listener from the event with the given {@code name}.
	 *
	 * @param name The event name
	 * @param runnable The event listener
	 * @see #addEventListener(String, GenericRunnable)
	 * @see #removeAllEventListeners(String)
	 */
	public synchronized void removeEventListener(String name, GenericRunnable runnable){
		EventListenersObj listeners = this.getEventListeners0(name, false);
		if(listeners != null){
			synchronized(listeners){
				listeners.list.remove(runnable);
				listeners.listChanged();
				if(this.fastAccessIdCounter < 0 && listeners.list.size() == 0)
					this.events.remove(name);
			}
		}
	}

	/**
	 * Removes all event listeners from the event with the given {@code name}.
	 *
	 * @param name The event name
	 * @see #removeEventListener(String, GenericRunnable)
	 */
	public synchronized void removeAllEventListeners(String name){
		if(this.fastAccessIdCounter >= 0){
			EventListenersObj listeners = this.getEventListeners0(name, false);
			if(listeners != null){
				synchronized(listeners){
					listeners.list.clear();
					listeners.list.trimToSize();
					listeners.listChanged();
				}
			}
		}else{
			this.events.remove(name);
		}
	}

	/**
	 * Returns the number of event listeners registered for the given event {@code name}.
	 *
	 * @param name The event name
	 * @return The number of listeners
	 */
	public synchronized int getEventListenerCount(String name){
		EventListenersObj listeners = this.getEventListeners0(name, false);
		return listeners != null ? listeners.list.size() : 0;
	}

	/**
	 * Returns an unmodifiable set of known event names. This includes names of events that were ever passed to {@link runEvent(String, Object...)}, {@link #addEventListener(String, GenericRunnable)}
	 * or similar methods. This <b>does not always</b> exclude event names for which no listeners are registered.
	 *
	 * @return The set of known event names
	 */
	public synchronized Set<String> getEventNames(){
		return Collections.unmodifiableSet(this.events.keySet());
	}

	/**
	 * Returns an unmodifiable list of the event listeners registered for the given event {@code name}.
	 *
	 * @param name The event name
	 * @return The list of event listeners
	 */
	public synchronized List<GenericRunnable> getEventListeners(String name){
		EventListenersObj listeners = this.getEventListeners0(name, false);
		return listeners != null ? Collections.unmodifiableList(listeners.list) : Collections.emptyList();
	}

	/**
	 * Returns {@code true} if the given event {@code name} is a known event.
	 *
	 * @param name The event name
	 * @return {@code true} if the given event {@code name} is a known event
	 * @see #getEventNames()
	 */
	public synchronized boolean isEventRegistered(String name){
		return this.events.containsKey(name);
	}


	/**
	 * If set to {@code true}, all listeners for an event will always be executed regardless of errors thrown at the listener-level. If set to {@code false}, event execution will be canceled at the
	 * first thrown exception. The default is {@code true}.
	 *
	 * @param coalesceListenerErrors {@code true} to always execute all listeners
	 * @see EventEmitter
	 */
	public void setCoalesceListenerErrors(boolean coalesceListenerErrors){
		this.coalesceListenerErrors = coalesceListenerErrors;
	}


	/**
	 * Dispatches an event with the given {@code name}.
	 * <p>
	 * The set of listeners executed for this event is determined once before the first listener is executed. As a consequence, removing a subsequent event listener in an event listener for the
	 * same event may still cause the removed event listener to be executed one last time.
	 *
	 * @param name The event name
	 * @param args The arguments to pass to the event listeners
	 * @return The number of registered listeners
	 * @throws ExecutionFailedException If at least one listener failed to execute
	 * @see EventEmitter
	 */
	public int runEvent(String name, Object... args){
		EventListenersObj listeners;
		synchronized(this){
			listeners = this.getEventListeners0(name, false);
		}
		return this.runListeners(name, listeners, args);
	}

	/**
	 * Dispatches an event with the given <i>fastAccess</i> event ID.
	 * <p>
	 * See {@link #runEvent(String, Object...)} for more information.
	 *
	 * @param id The event ID
	 * @param args The arguments to pass to the event listeners
	 * @return The number of registered listeners
	 * @throws IllegalStateException If <i>fastAccess</i> is not enabled
	 * @throws IllegalArgumentException If the given {@code id} is not a valid ID
	 * @throws ExecutionFailedException If at least one listener failed to execute
	 * @see #createEventId(String, int)
	 * @see EventEmitter
	 */
	public int runEvent(int id, Object... args){
		if(this.fastAccessEventHandlers == null || this.fastAccessIdCounter < 0)
			throw new IllegalStateException("fastAccess is not enabled");
		if(id < 0 || id >= this.fastAccessEventHandlers.length)
			throw new IllegalArgumentException("Invalid event id: " + id);
		return this.runListeners(id, this.fastAccessEventHandlers[id], args);
	}

	private int runListeners(Object dbgName, EventListenersObj listeners, Object... args){
		if(listeners == null)
			return 0;
		List<ExecutionFailedException> execErrors = null;
		List<GenericRunnable> listenerRunnables = listeners.getRunList();
		int total = listenerRunnables.size();
		for(GenericRunnable runnable : listenerRunnables){
			try{
				runnable.run(args);
			}catch(Exception e){
				ExecutionFailedException wrapE = new ExecutionFailedException("Error executing event listener " + runnable + " for event '" + dbgName + "'[" + args.length + "]", e);
				if(this.coalesceListenerErrors){
					if(execErrors == null)
						execErrors = new ArrayList<>();
					execErrors.add(wrapE);
				}else
					throw wrapE;
			}
		}
		if(execErrors != null){
			if(execErrors.size() == 1)
				throw execErrors.get(0);
			ExecutionFailedException err = new ExecutionFailedException(execErrors.size() + " of " + total + " event listeners failed to execute succesfully");
			for(Throwable e : execErrors)
				err.addSuppressed(e);
			throw err;
		}
		return total;
	}


	private class OnceRunnable implements GenericRunnable {

		private final String name;
		private final GenericRunnable runnable;

		public OnceRunnable(String name, GenericRunnable runnable){
			this.name = name;
			this.runnable = runnable;
			EventEmitter.this.addEventListener(this.name, this);
		}


		@Override
		public void run(Object... args) throws Exception {
			EventEmitter.this.removeEventListener(this.name, this);
			this.runnable.run(args);
		}

		@Override
		public int getArgumentCount(){
			return this.runnable.getArgumentCount();
		}

		@Override
		public String toString(){
			return "[ONCE] " + this.runnable.toString();
		}
	}


	private static class EventListenersObj {

		public final ArrayList<GenericRunnable> list;
		public ArrayList<GenericRunnable> runList;

		public EventListenersObj(){
			this.list = new ArrayList<GenericRunnable>(1);
		}


		public void listChanged(){
			assert Thread.holdsLock(this);
			this.runList = null;
		}

		@SuppressWarnings("unchecked")
		public synchronized ArrayList<GenericRunnable> getRunList(){
			if(this.runList == null)
				this.runList = (ArrayList<GenericRunnable>) this.list.clone();
			return this.runList;
		}
	}
}
