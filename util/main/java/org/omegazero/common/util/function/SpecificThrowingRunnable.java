/*
 * Copyright (C) 2023 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util.function;

/**
 * Similar to a {@link Runnable}, except that the functional {@code run} method is allowed to throw the specified exception.
 *
 * @param <E> The exception type
 * @since 2.11.0
 */
@FunctionalInterface
public interface SpecificThrowingRunnable<E extends Throwable> {

	/**
	 * Runs this {@code ThrowingRunnable}.
	 *
	 * @throws E Any exception
	 */
	public void run() throws E;
}
