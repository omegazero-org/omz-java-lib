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
	 * The value of the optional priority parameter. Default: {@link Priority#NORMAL}.
	 *
	 * @return The priority
	 */
	Priority priority() default Priority.NORMAL;

	/**
	 * Contains priority values for {@link SubscribeEvent#priority()}.
	 */
	public enum Priority {
		LOWEST(0), LOW(536870911), NORMAL(1073741823), HIGH(1610612735), HIGHEST(2147483647);

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
