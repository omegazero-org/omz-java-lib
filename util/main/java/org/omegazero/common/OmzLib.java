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

/**
 * This class contains metadata about <i>omz-java-lib</i> (for example the {@linkplain #VERSION version string}).
 */
public class OmzLib {

	/**
	 * The version string of <i>omz-java-lib</i>.<br>
	 * <br>
	 * This value is set by the CI build pipeline based on the event that triggered the build. Otherwise, this string is always <code>"$BUILDVERSION"</code>.
	 */
	public static final String VERSION = "$BUILDVERSION";


	/**
	 * Prints the version string and configured log level using a <code>Logger</code> from the <i>logging</i> library. If the library is not available, the string is printed
	 * to <code>System.err</code>.
	 */
	public static void printBrand() {
		String s = "omz-java-lib version " + OmzLib.VERSION;
		try{
			Class<?> loggerUtilCl = Class.forName("org.omegazero.common.logging.LoggerUtil");
			Class<?> loggerCl = Class.forName("org.omegazero.common.logging.Logger");
			Object logger = loggerCl.getMethod("create").invoke(null);
			loggerCl.getMethod("info", Object[].class).invoke(logger, (Object) new Object[] { s, " (log level: ", loggerUtilCl.getMethod("getLogLevel").invoke(null), ")" });
		}catch(ReflectiveOperationException e){
			System.err.println(s);
		}
	}
}
