/*
 * Copyright (C) 2023 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util.function;

/**
 * Similar to a {@link java.util.function.Consumer}, except that the functional {@code accept} method is allowed to throw the specified exception.
 *
 * @param <E> The exception type
 * @param <T> The input type
 * @since 2.11.0
 */
@FunctionalInterface
public interface SpecificThrowingConsumer<E extends Throwable, T> {

	/**
	 * Runs this {@code ThrowingConsumer}.
	 *
	 * @param t The input argument
	 * @throws E Any exception
	 */
	public void accept(T t) throws E;
}
