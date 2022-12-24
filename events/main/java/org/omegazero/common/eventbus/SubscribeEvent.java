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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation required on methods to be identified as a valid {@link EventBus} subscriber event handler method.
 * 
 * @since 2.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface SubscribeEvent {

	/**
	 * An optional priority parameter. Default: {@link Priority#NORMAL}.
	 *
	 * @return The priority value
	 */
	Priority priority() default Priority.NORMAL;

	/**
	 * An alternative numeric value for the priority parameter. If this value is set to anything other than the numeric value of {@link Priority#NORMAL} (the default),
	 * it takes precendence over {@link #priority()}; otherwise, the numeric value of {@link #priority()} is used.
	 *
	 * @return The custom numeric priority
	 * @since 2.10.1
	 */
	int priorityNum() default 1073741823;

	/**
	 * Contains priority values for {@link SubscribeEvent#priority()}.
	 */
	public enum Priority {

		/**
		 * The lowest possible priority value.
		 */
		LOWEST(0),
		/**
		 * Priority value between {@code LOWEST} and {@code NORMAL}.
		 */
		LOW(536870911),
		/**
		 * The default priority.
		 */
		NORMAL(1073741823),
		/**
		 * Priority value between {@code NORMAL} and {@code HIGHEST}.
		 */
		HIGH(1610612735),
		/**
		 * The highest possible priority value.
		 */
		HIGHEST(2147483647);

		private final int value;

		private Priority(int value) {
			this.value = value;
		}

		/**
		 * The {@code int} value of this {@code Priority} value.
		 *
		 * @return The numeric value
		 */
		public int value(){
			return this.value;
		}
	}
}
