/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.plugins;

/**
 * An exception thrown to indicate a {@link Plugin} is invalid.
 * 
 * @since 2.2
 */
public class InvalidPluginException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPluginException(String name, String msg) {
		this(name, msg, null);
	}

	public InvalidPluginException(String name, String msg, Throwable cause) {
		super("Plugin '" + name + "': " + msg, cause);
	}
}
