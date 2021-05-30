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

public class EventBus {

	private final List<Subscriber> subscribers = new ArrayList<>();

	private final Map<String, List<Subscriber>> eventCache = new HashMap<>();


	/**
	 * Adds the <b>instance</b> to the list of listeners receiving events from the event bus.<br>
	 * <br>
	 * The class is derived from the given <b>instance</b> parameter.
	 * 
	 * @param instance
	 */
	public void register(Object instance, String... forcedEvents) {
		this.register(new Subscriber(instance), forcedEvents);
	}

	/**
	 * Adds a listener for type <b>type</b> to the list of listeners receiving events from the event bus.<br>
	 * <br>
	 * Note that non-static methods cannot be used as listeners using this method. To use non-static methods, pass an instance with {@link EventBus#register(Class, Object)} or
	 * {@link EventBus#register(Object)}.
	 * 
	 * @param type
	 * @param instance
	 */
	public void register(Class<?> type, String... forcedEvents) {
		this.register(new Subscriber(type), forcedEvents);
	}

	/**
	 * Adds the <b>instance</b> of type <b>type</b> to the list of listeners receiving events from the event bus.
	 * 
	 * @param type
	 * @param instance
	 */
	public void register(Class<?> type, Object instance, String... forcedEvents) {
		this.register(new Subscriber(type, instance), forcedEvents);
	}

	/**
	 * Adds the event bus subscriber to the list of listeners receiving events from the event bus.
	 * 
	 * @param subscriber
	 */
	public synchronized void register(Subscriber subscriber, String... forcedEvents) {
		if(forcedEvents.length > 0)
			subscriber.setForcedEvents(forcedEvents);
		subscribers.add(subscriber);
		this.flushEventCache();
	}

	/**
	 * Removes the event bus subscriber with <b>instance</b> from the list of listeners receiving events from the event bus.
	 * 
	 * @param instance
	 */
	public synchronized void unregister(Object instance) {
		boolean b = false;
		for(int i = 0; i < subscribers.size(); i++)
			if(subscribers.get(i).equals(instance)){
				subscribers.remove(i);
				b = true;
			}
		if(b)
			this.flushEventCache();
	}

	/**
	 * Removes all event bus subscribers of the given type from the list of listeners receiving events from the event bus.
	 * 
	 * @param type
	 */
	public synchronized void unregister(Class<?> type) {
		boolean b = false;
		for(int i = 0; i < subscribers.size(); i++)
			if(subscribers.get(i).getType().equals(type)){
				subscribers.remove(i);
				b = true;
			}
		if(b)
			this.flushEventCache();
	}


	/**
	 * Flushes the event cache for all events.<br>
	 * <br>
	 * The next time an event is dispatched, the list of listeners must be rebuilt from the list of subscribers.
	 */
	public void flushEventCache() {
		eventCache.clear();
	}


	/**
	 * @return The number of registered event bus subscribers.
	 */
	public int getSubscriberCount() {
		return subscribers.size();
	}


	/**
	 * Dispatches the given <b>event</b> to all event bus subscribers listening for this event.<br>
	 * <br>
	 * An event bus subscriber is listening for an event if its class contains a method with the name given in the event instance, matching parameters and the
	 * {@link SubscribeEvent} annotation. The {@link SubscribeEvent} annotation may contain the optional argument {@link SubscribeEvent#priority()}. The handlers with priority
	 * <i>HIGHEST</i> will be invoked first, the handlers with priority <i>LOWEST</i> will be executed last.<br>
	 * <br>
	 * During execution of this event, any handler may cancel the event using {@link Event#cancel()} if {@link Event#isCancelable()} is <code>true</code>, in which case the
	 * event will be stopped being delivered to subsequent event handlers.<br>
	 * <br>
	 * If and {@link Event#isIncludeAllReturns()} is <code>false</code>, event delivery will be stopped as soon as the first listener returns a non-<code>null</code> value. If
	 * the event return type is <code>void</code>, all listeners will be attempted to be executed, because <code>void</code> methods effectively always return
	 * <code>null</code>.<br>
	 * <br>
	 * This method may be used instead of {@link EventBus#dispatchEventRes(Event, Object...)} if return types are not needed to save resources, because no
	 * <code>EventResult</code> object is being created.
	 * 
	 * @param event The event to be executed among all subscribers listening to this event
	 * @param args  Arguments to be passed to event listeners
	 * @throws EventBusException If an error occurs during execution of an event handler
	 * @return The number of executed event handlers
	 * @see EventBus#dispatchEventRes(Event, Object...)
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
	 * @see EventBus#dispatchEvent(Event, Object...)
	 */
	public EventResult dispatchEventRes(Event event, Object... args) {
		EventResult res = new EventResult();
		int c = this.dispatchEvent0(event, res, args);
		res.setListeners(c);
		return res;
	}

	private synchronized int dispatchEvent0(Event event, EventResult res, Object[] args) {
		List<Subscriber> subs = eventCache.get(event.getEventSignature());
		if(subs == null){
			subs = new ArrayList<Subscriber>();
			for(Subscriber sub : subscribers){
				boolean av = sub.isListenerMethodForEventAvailable(event, args);
				if(av)
					subs.add(sub);
				else if(sub.isForcedEvent(event))
					throw new EventBusException("Subscriber '" + sub.getType().getName() + "' declared itself as listening for event '" + event.getEventSignature()
							+ "', but no suitable handler method was found");
			}
			subs.sort(new Comparator<Subscriber>(){

				@Override
				public int compare(Subscriber s1, Subscriber s2) {
					return s2.getListenerMethodForEvent(event, args).getAnnotation(SubscribeEvent.class).priority().value()
							- s1.getListenerMethodForEvent(event, args).getAnnotation(SubscribeEvent.class).priority().value();
				}
			});
			eventCache.put(event.getEventSignature(), subs);
		}
		int listeners = 0;
		for(Subscriber sub : subs){
			try{
				Object ret = sub.runEvent(event, args);
				listeners++;
				if(ret != null){
					if(event.isIncludeAllReturns()){
						if(res != null)
							res.getReturnValues().add(ret);
					}else{
						if(res != null)
							res.setReturnValue(ret);
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
