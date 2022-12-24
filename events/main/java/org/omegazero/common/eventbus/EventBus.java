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
package org.omegazero.common.eventbus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An <code>EventBus</code> is used for {@linkplain #dispatchEvent(Event, Object...) dispatching} <code>Event</code>s to event bus {@linkplain Subscriber subscribers} which
 * are listening for the specific event and {@linkplain #register(Subscriber, String...) registered} in this <code>EventBus</code>.
 * <p>
 * An event bus subscriber is listening for an event if its class contains a method with the name given in the <code>Event</code> instance, matching parameters and the
 * {@link SubscribeEvent} annotation. The {@link SubscribeEvent} annotation may contain the optional argument {@link SubscribeEvent#priority()}. The handlers with priority
 * <i>HIGHEST</i> will be invoked first, the handlers with priority <i>LOWEST</i> will be executed last. The order of execution of multiple handlers with the same priority is
 * undefined. An event bus subscriber can {@linkplain Subscriber#setForcedEvents(String[]) declare itself} as listening for specific events. If an attempt is made to execute
 * an event but no handler method is found in the subscriber class, event execution will fail.
 * <p>
 * During execution of this event, any handler may cancel the event using {@link Event#cancel()} if {@link Event#isCancelable()} is <code>true</code>, in which case the event
 * will be stopped being delivered to subsequent event handlers. If {@link Event#isIncludeAllReturns()} is <code>false</code>, event delivery will be stopped as soon as the
 * first listener returns a non-<code>null</code> value. If the event return type is <code>void</code>, all listeners will be attempted to be executed, because
 * <code>void</code> methods always effectively return <code>null</code>.
 * 
 * @since 2.1
 */
public class EventBus {

	private final List<Subscriber> subscribers = new ArrayList<>();

	private final Map<String, List<Subscriber>> eventCache = new HashMap<>();


	/**
	 * Adds the <b>instance</b> to the list of listeners receiving events from the event bus.
	 * <p>
	 * The class is derived from the given <b>instance</b> parameter.
	 * 
	 * @param instance The instance of the event bus subscriber. The class of the instance must have the {@link EventBusSubscriber} annotation
	 * @param forcedEvents The list of event names the caller asserts that the given event bus subscriber is listening to
	 */
	public void register(Object instance, String... forcedEvents) {
		this.register(new Subscriber(instance), forcedEvents);
	}

	/**
	 * Adds a listener for type <b>type</b> to the list of listeners receiving events from the event bus.
	 * <p>
	 * Note that non-static methods cannot be used as listeners using this method. To use non-static methods, pass an instance with {@link #register(Class, Object, String[])} or
	 * {@link #register(Object, String[])}.
	 * 
	 * @param type The type of the event bus subscriber. This class must have the {@link EventBusSubscriber} annotation
	 * @param forcedEvents The list of event names the caller asserts that the given event bus subscriber is listening to
	 */
	public void register(Class<?> type, String... forcedEvents) {
		this.register(new Subscriber(type), forcedEvents);
	}

	/**
	 * Adds the <b>instance</b> of type <b>type</b> to the list of listeners receiving events from the event bus.
	 * 
	 * @param type     The type of the event bus subscriber. This class must have the {@link EventBusSubscriber} annotation
	 * @param instance The instance of the event bus subscriber
	 * @param forcedEvents The list of event names the caller asserts that the given event bus subscriber is listening to
	 */
	public void register(Class<?> type, Object instance, String... forcedEvents) {
		this.register(new Subscriber(type, instance), forcedEvents);
	}

	/**
	 * Adds the event bus subscriber to the list of listeners receiving events from the event bus.
	 * 
	 * @param subscriber The event bus subscriber
	 * @param forcedEvents The list of event names the caller asserts that the given event bus subscriber is listening to
	 */
	public synchronized void register(Subscriber subscriber, String... forcedEvents) {
		if(this.subscribers.contains(subscriber))
			throw new IllegalStateException("The given subscriber is already registered");
		if(forcedEvents.length > 0)
			subscriber.setForcedEvents(forcedEvents);
		this.subscribers.add(subscriber);
		this.flushEventCache();
	}

	/**
	 * Removes the given event bus subscriber from the list of listeners receiving events from the event bus.
	 * 
	 * @param subscriber The event bus subscriber instance
	 * @return <code>true</code> if the given <b>subscriber</b> was registered
	 * @since 2.7
	 */
	public synchronized boolean unregister(Subscriber subscriber) {
		if(this.subscribers.remove(subscriber)){
			this.flushEventCache();
			return true;
		}
		return false;
	}

	/**
	 * Removes the event bus subscriber with <b>instance</b> from the list of listeners receiving events from the event bus. This method effectively does nothing if the given
	 * <b>instance</b> is not the instance of a registered event bus subscriber.
	 * 
	 * @param instance The event bus subscriber instance
	 */
	public synchronized void unregister(Object instance) {
		if(this.subscribers.removeIf((sub) -> {
			return sub.getInstance().equals(instance);
		}))
			this.flushEventCache();
	}

	/**
	 * Removes all event bus subscribers of the given type from the list of listeners receiving events from the event bus. This method effectively does nothing if the given
	 * <b>instance</b> is not a registered event bus subscriber.
	 * 
	 * @param type The type
	 */
	public synchronized void unregister(Class<?> type) {
		if(this.subscribers.removeIf((sub) -> {
			return sub.getType().equals(type);
		}))
			this.flushEventCache();
	}


	/**
	 * Flushes the event cache for all events.
	 * <p>
	 * The next time an event is dispatched, the list of listeners must be rebuilt from the list of subscribers.
	 */
	public synchronized void flushEventCache() {
		this.eventCache.clear();
	}


	/**
	 * Returns the number of registered event bus subscribers.
	 * 
	 * @return The number of registered event bus subscribers
	 */
	public int getSubscriberCount() {
		return this.subscribers.size();
	}


	/**
	 * Dispatches the given <b>event</b> to all event bus subscribers listening for this event.
	 * <p>
	 * This method may be used instead of {@link EventBus#dispatchEventRes(Event, Object...)} if return types are not needed to save resources, because no
	 * <code>EventResult</code> object is being created.
	 * 
	 * @param event The event to be executed among all subscribers listening to this event
	 * @param args  Arguments to be passed to event listeners
	 * @throws EventBusException If an error occurs during execution of an event handler
	 * @return The number of executed event handlers
	 * @see #dispatchEventRes(Event, Object...)
	 * @see EventBus
	 */
	public int dispatchEvent(Event event, Object... args) {
		return this.dispatchEvent0(event, null, args);
	}

	/**
	 * The behavior of this method is equal to {@link EventBus#dispatchEvent(Event, Object...)}, except that this method also returns the return values of the listener
	 * methods, wrapped in an {@link EventResult} object.
	 * 
	 * @param event The event to be executed among all subscribers listening to this event
	 * @param args  Arguments to be passed to event listeners
	 * @throws EventBusException If an error occurs during execution of an event handler
	 * @return An <code>EventResult</code> object containing data about this event dispatch
	 * @see #dispatchEvent(Event, Object...)
	 * @see EventBus
	 */
	public EventResult dispatchEventRes(Event event, Object... args) {
		EventResult res = new EventResult();
		int c = this.dispatchEvent0(event, res, args);
		res.listeners = c;
		return res;
	}

	private synchronized List<Subscriber> getSortedEventSubscriberList(Event event, Object[] args) {
		List<Subscriber> subs = this.eventCache.get(event.getEventSignature());
		if(subs == null){
			subs = new ArrayList<Subscriber>();
			for(Subscriber sub : this.subscribers){
				boolean av = sub.isListenerMethodForEventAvailable(event);
				if(av)
					subs.add(sub);
				else if(sub.isForcedEvent(event))
					throw new EventBusException("Subscriber '" + sub.getType().getName() + "' declared itself as listening for event '" + event.getEventSignature()
							+ "', but no suitable handler method was found");
			}
			subs.sort(new Comparator<Subscriber>(){

				@Override
				public int compare(Subscriber s1, Subscriber s2) {
					SubscribeEvent s1a = s1.getListenerMethodForEvent(event).getAnnotation(SubscribeEvent.class);
					SubscribeEvent s2a = s2.getListenerMethodForEvent(event).getAnnotation(SubscribeEvent.class);
					int s1p = s1a.priorityNum() != SubscribeEvent.Priority.NORMAL.value() ? s1a.priorityNum() : s1a.priority().value();
					int s2p = s2a.priorityNum() != SubscribeEvent.Priority.NORMAL.value() ? s2a.priorityNum() : s2a.priority().value();
					return s2p - s1p;
				}
			});
			this.eventCache.put(event.getEventSignature(), subs);
		}
		return subs;
	}

	private int dispatchEvent0(Event event, EventResult res, Object[] args) {
		List<Subscriber> subs = this.getSortedEventSubscriberList(event, args);
		int listeners = 0;
		if(event.isIncludeAllReturns())
			res.returnValues = new ArrayList<>();
		for(Subscriber sub : subs){
			try{
				Object ret = sub.runEvent(event, args);
				listeners++;
				if(ret != null){
					if(event.isIncludeAllReturns()){
						if(res != null)
							res.returnValues.add(ret);
					}else{
						if(res != null)
							res.returnValue = ret;
						break;
					}
				}
				if(event.isCanceled())
					break;
			}catch(Exception e){
				throw new EventBusException("Error while running event '" + event.getEventSignature() + "' at " + sub.getType().getName(), e);
			}
		}
		return listeners;
	}
}
