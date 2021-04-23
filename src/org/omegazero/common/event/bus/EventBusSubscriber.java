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
package org.omegazero.common.event.bus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.omegazero.common.util.ReflectionUtil;

public class EventBusSubscriber {

	private final Class<?> type;
	private Object instance;

	private String[] forcedEvents = null;

	private final Map<String, Method> methodCache = new HashMap<>();

	/**
	 * Alias for {@link EventBusSubscriber#EventBusSubscriber(Class, Object)}.<br>
	 * <br>
	 * The <b>type</b> parameter is derived from the given <b>instance</b> parameter.
	 * 
	 * @param instance
	 */
	public EventBusSubscriber(Object instance) {
		this(instance.getClass(), instance);
	}

	/**
	 * Creates an EventBusSubscriber instance for use with the {@link EventBus}.<br>
	 * <br>
	 * The class <b>type</b> must have the annotation {@link IEventBusSubscriber} set.<br>
	 * <br>
	 * Only static event listener methods can be called when using this constructor. To be able to use non-static methods, pass an instance with
	 * {@link EventBusSubscriber#EventBusSubscriber(Class, Object)} or {@link EventBusSubscriber#EventBusSubscriber(Object)}.
	 * 
	 * @param instance
	 */
	public EventBusSubscriber(Class<?> type) {
		this(type, null);
	}

	/**
	 * Creates an EventBusSubscriber instance for use with the {@link EventBus}.<br>
	 * <br>
	 * The class <b>type</b> must have the annotation {@link IEventBusSubscriber} set.<br>
	 * <br>
	 * The object given in <b>instance</b> will be the instance the method is called with.
	 * 
	 * @param type
	 * @param instance
	 */
	public EventBusSubscriber(Class<?> type, Object instance) {
		this.type = Objects.requireNonNull(type);
		if(!type.isAnnotationPresent(IEventBusSubscriber.class))
			throw new IllegalArgumentException("Class does not have @IEventBusSubscriber annotation");
		this.instance = instance;
		if(instance != null && !instance.getClass().equals(type))
			throw new IllegalArgumentException("Instance is not of type '" + type.getName() + "'");
	}


	/**
	 * Returns <b>true</b> if the given method <b>m</b> is a valid event listener method.
	 * 
	 * @param m The method to validate
	 * @return <b>true</b> if <b>m</b> is a valid event listener method
	 */
	public boolean isValidListenerMethod(Method m) {
		return m.getReturnType() == void.class && m.isAnnotationPresent(SubscribeEvent.class);
	}

	/**
	 * Searches the valid event listener method suitable for the given <b>event</b>.<br>
	 * <br>
	 * The result of this method will be cached, including when a method was not found.
	 * 
	 * @param event The event to find a suitable method for
	 * @param args
	 * @return The valid event listener method, or <b>null</b> if the method was not found
	 */
	public Method getListenerMethodForEvent(Event event, Object... args) {
		return this.getListenerMethodWSig(event.getMethodName(), event.getParams(), event.getEventSignature());
	}

	/**
	 * Checks if there is a valid listener method available at this event bus subscriber.
	 * 
	 * @param event The event to check
	 * @param args
	 * @return <b>true</b> if a valid listener method for this event was found, <b>false</b> otherwise
	 */
	public boolean isListenerMethodForEventAvailable(Event event, Object... args) {
		return this.getListenerMethodForEvent(event, args) != null;
	}


	/**
	 * Searches the valid event listener method with the given <b>name</b> and <b>parameterTypes</b>. Returns <b>null</b> if a suitable method was not found.<br>
	 * <br>
	 * The result of this method will be cached, including when a method was not found.
	 * 
	 * @param name           The name of the method to be searched
	 * @param parameterTypes An array of parameter types the method should have
	 * @return The valid event listener method, or <b>null</b> if the method was not found
	 */
	public Method getListenerMethod(String name, Class<?>[] parameterTypes) {
		return this.getListenerMethodWSig(name, parameterTypes, Event.createEventSignature(name, parameterTypes));
	}


	private Method getListenerMethodWSig(String name, Class<?>[] parameterTypes, String eventSig) {
		Method method = null;
		if(methodCache.containsKey(eventSig)){
			method = methodCache.get(eventSig);
		}else{
			Method[] methods = this.type.getMethods();
			for(Method m : methods){
				if(ReflectionUtil.isMethod(m, name, parameterTypes) && isValidListenerMethod(m)){
					method = m;
					break;
				}
			}
			methodCache.put(eventSig, method);
		}
		return method;
	}


	public Class<?> getType() {
		return this.type;
	}

	protected Object getInstance() {
		return this.instance;
	}


	public String[] getForcedEvents() {
		return forcedEvents;
	}

	public void setForcedEvents(String[] forcedEvents) {
		this.forcedEvents = forcedEvents;
	}


	public boolean isForcedEvent(Event event) {
		if(this.forcedEvents == null)
			return false;
		for(String s : this.forcedEvents){
			if(s.equals(event.getName()))
				return true;
		}
		return false;
	}

	/**
	 * Gets the event handler method for the passed <b>event</b> and invokes it with the given arguments.<br>
	 * <br>
	 * A handler method must have the annotation {@link SubscribeEvent} with the optional {@link SubscribeEvent#priority()} argument and must be of return type void.
	 * 
	 * @param event The event to be dispatched to the event bus subscriber.
	 * @param args  Arguments to be passed to the event method.
	 * @throws ReflectiveOperationException
	 * @see {@link EventBus#dispatchEvent(Event, Object...)}
	 */
	public boolean dispatchEvent(Event event, Object... args) throws ReflectiveOperationException {
		Method m = this.getListenerMethodForEvent(event, args);
		if(m == null)
			return false;
		this.dispatchEventMethod(m, args);
		return true;
	}

	/**
	 * Validates and invokes the method <b>eventListener</b> with the given arguments.<br>
	 * <br>
	 * <b>eventListener</b> must be a valid event listener. See {@link EventBusSubscriber#dispatchEvent(Event, Object...)}.
	 * 
	 * @param eventListener
	 * @param args
	 * @throws ReflectiveOperationException
	 */
	public void dispatchEventMethod(Method eventListener, Object... args) throws ReflectiveOperationException {
		if(!isValidListenerMethod(eventListener))
			throw new RuntimeException("Invalid eventListener method");
		eventListener.invoke(this.instance, args);
	}
}
