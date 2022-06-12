/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.runtime;

import org.omegazero.common.util.Args;

/**
 * Represents an application managed by the {@link ApplicationWrapper}.
 * 
 * @since 2.9
 */
public interface Application {


	/**
	 * Starts the application.
	 * 
	 * @param args The startup arguments
	 * @throws Exception Any exception thrown during startup
	 * @see ApplicationWrapper#start(String[])
	 */
	public void start(Args args) throws Exception;

	/**
	 * Stops the application
	 * 
	 * @throws Exception Any exception thrown during shutdown
	 * @see ApplicationWrapper#shutdown()
	 */
	public void close() throws Exception;
}
