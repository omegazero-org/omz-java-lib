/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util.function;

/**
 * Similar to a {@link Runnable}, except that the functional {@code run} method is allowed to throw any exception.
 *
 * @since 2.9
 */
@FunctionalInterface
public interface ThrowingRunnable extends SpecificThrowingRunnable<Exception> {
}
