/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util.function;

/**
 * Similar to a {@link java.util.function.Supplier}, except that the functional {@code get} method is allowed to throw any exception.
 * 
 * @param <R> The result type
 * @since 2.9
 */
@FunctionalInterface
public interface ThrowingSupplier<R> {

	/**
	 * Creates a result.
	 * 
	 * @return The result
	 * @throws Exception Any exception
	 */
	public R get() throws Exception;
}
