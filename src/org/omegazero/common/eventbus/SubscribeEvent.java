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

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface SubscribeEvent {

	/**
	 * The value of the optional priority parameter.<br>
	 * The default value is NORMAL.
	 */
	Priority priority() default Priority.NORMAL;

	public enum Priority {
		LOWEST(0), LOW(536870911), NORMAL(1073741823), HIGH(1610612735), HIGHEST(2147483647);

		private final int VALUE;

		private Priority(int value) {
			this.VALUE = value;
		}

		public int value() {
			return VALUE;
		}
	}
}
