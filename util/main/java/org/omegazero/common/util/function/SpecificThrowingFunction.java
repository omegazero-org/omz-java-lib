/*
 * Copyright (C) 2023 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util.function;

/**
 * Similar to a {@link java.util.function.Function}, except that the functional {@code apply} method is allowed to throw the specified exception.
 *
 * @param <E> The exception type
 * @param <T> The input type
 * @param <R> The result type
 * @since 2.11.0
 */
@FunctionalInterface
public interface SpecificThrowingFunction<E extends Throwable, T, R> {

	/**
	 * Performs an operation on the given object and returns its result.
	 *
	 * @param t The input argument
	 * @return The result
	 * @throws E Any exception
	 */
	public R apply(T t) throws E;
}
