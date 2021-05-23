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
package org.omegazero.common;

import org.omegazero.common.logging.Logger;
import org.omegazero.common.logging.LoggerUtil;

public class OmzLib {

	public static final String VERSION = "2.1";


	private static final Logger logger = LoggerUtil.createLogger();

	public static void printBrand() {
		logger.info("omz-java-lib version " + OmzLib.VERSION + " (log level " + LoggerUtil.getLogLevel().name() + ")");
	}
}
