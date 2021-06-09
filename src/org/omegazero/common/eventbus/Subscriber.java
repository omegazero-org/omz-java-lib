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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.omegazero.common.util.ReflectionUtil;

public class Subscriber {

	private final Class<?> type;
	private Object instance;

	private String[] forcedEvents = null;

	private final Map<String, Method> methodCache = new HashMap<>();

	/**
	 * Alias for {@link Subscriber#Subscriber(Class, Object)}.<br>
	 * <br>
	 * The <b>type</b> parameter is derived from the given <b>instance</b> parameter.
	 * 
	 * @param instance
	 */
	public Subscriber(Object instance) {
		this(instance.getClass(), instance);
	}

	/**
	 * Creates an <code>Subscriber</code> instance for use with the {@link EventBus}.<br>
	 * <br>
	 * The class <b>type</b> must have the annotation {@link EventBusSubscriber} set.<br>
	 * <br>
	 * Only static event listener methods can be called when using this constructor. To be able to use non-static methods, pass an instance with
	 * {@link Subscriber#Subscriber(Class, Object)} or {@link Subscriber#Subscriber(Object)}.
	 * 
	 * @param instance
	 */
	public Subscriber(Class<?> type) {
		this(type, null);
	}

	/**
	 * Creates an <code>Subscriber</code> instance for use with the {@link EventBus}.<br>
	 * <br>
	 * The class <b>type</b> must have the annotation {@link EventBusSubscriber} set.<br>
	 * <br>
	 * The object given in <b>instance</b> will be the instance the method is called with.
	 * 
	 * @param type
	 * @param instance
	 */
	public Subscriber(Class<?> type, Object instance) {
		this.type = Objects.requireNonNull(type);
		if(!type.isAnnotationPresent(EventBusSubscriber.class))
			throw new IllegalArgumentException("Class does not have @EventBusSubscriber annotation");
		this.instance = instance;
		if(instance != null && !instance.getClass().equals(type))
			throw new IllegalArgumentException("Instance is not of type '" + type.getName() + "'");
	}


	/**
	 * Returns <code>true</code> if the given method <b>m</b> is a valid event listener method.
	 * 
	 * @param m The method to validate
	 * @return <code>true</code> if <b>m</b> is a valid event listener method
	 */
	public boolean isValidListenerMethod(Method m) {
		return m.isAnnotationPresent(SubscribeEvent.class);
	}

	/**
	 * Searches the valid event listener method suitable for the given <b>event</b>.<br>
	 * <br>
	 * The result of this method will be cached, including when a method was not found.
	 * 
	 * @param event The event to find a suitable method for
	 * @param args
	 * @return The valid event listener method, or <code>null</code> if the method was not found
	 */
	public Method getListenerMethodForEvent(Event event, Object... args) {
		return this.getListenerMethodWSig(event.getMethodName(), event.getParams(), event.getReturnType(), event.getEventSignature());
	}

	/**
	 * Checks if there is a valid listener method available at this event bus subscriber.
	 * 
	 * @param event The event to check
	 * @param args
	 * @return <code>true</code> if a valid listener method for this event was found, <b>false</b> otherwise
	 */
	public boolean isListenerMethodForEventAvailable(Event event, Object... args) {
		return this.getListenerMethodForEvent(event, args) != null;
	}


	/**
	 * Searches the valid event listener method with the given <b>name</b> and <b>parameterTypes</b>. Returns <code>null</code> if a suitable method was not found.<br>
	 * <br>
	 * The result of this method will be cached, including when a method was not found.
	 * 
	 * @param name           The name of the method to be searched
	 * @param parameterTypes An array of parameter types the method should have
	 * @return The valid event listener method, or <code>null</code> if the method was not found
	 */
	public Method getListenerMethod(String name, Class<?>[] parameterTypes, Class<?> returnType) {
		return this.getListenerMethodWSig(name, parameterTypes, returnType, Event.createEventSignature(name, parameterTypes, returnType));
	}


	private Method getListenerMethodWSig(String name, Class<?>[] parameterTypes, Class<?> returnType, String eventSig) {
		Method method = null;
		if(methodCache.containsKey(eventSig)){
			method = methodCache.get(eventSig);
		}else{
			Method[] methods = this.type.getMethods();
			for(Method m : methods){
				if(ReflectionUtil.isMethod(m, name, parameterTypes) && (m.getReturnType() == returnType || m.getReturnType() == void.class) && isValidListenerMethod(m)){
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
			if(s.equals(event.getMethodName()))
				return true;
		}
		return false;
	}


	/**
	 * Gets the event handler method for the passed <b>event</b> and invokes it with the given arguments.<br>
	 * <br>
	 * A handler method must have the annotation {@link SubscribeEvent} with the optional {@link SubscribeEvent#priority()} argument.
	 * 
	 * @param event The event to be dispatched to the event bus subscriber.
	 * @param args
	 * @throws ReflectiveOperationException
	 * @see {@link EventBus#dispatchEvent(Event, Object...)}
	 */
	public Object runEvent(Event event, Object... args) throws ReflectiveOperationException {
		Method m = this.getListenerMethodForEvent(event, args);
		if(m == null)
			throw new NoSuchMethodException("No listener method for event '" + event.getMethodName() + "'");
		return this.runEventMethod(m, args);
	}

	/**
	 * Validates and invokes the method <b>eventListener</b> with the given arguments.<br>
	 * <br>
	 * <b>eventListener</b> must be a valid event listener. See {@link Subscriber#runEvent(Event, Object...)}.
	 * 
	 * @param eventListener A valid event listener method.
	 * @param args          Arguments to be passed to the event method.
	 * @throws ReflectiveOperationException
	 */
	public Object runEventMethod(Method eventListener, Object... args) throws ReflectiveOperationException {
		if(!isValidListenerMethod(eventListener))
			throw new RuntimeException("Invalid eventListener method");
		return eventListener.invoke(this.instance, args);
	}
}