/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.plugins;

public class InvalidPluginException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPluginException(String name, String msg) {
		super("Plugin '" + name + "': " + msg);
	}
}
