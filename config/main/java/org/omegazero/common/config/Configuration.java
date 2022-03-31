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
package org.omegazero.common.config;

import java.io.IOException;

/**
 * An interface for managing configuration files or similar.
 * <p>
 * Classes that implement this method usually support a single configuration format. Application classes then extend from those classes and declare several fields with the
 * {@link ConfigurationOption} annotation, which will be populated by a call to {@link #load()}, with values from the configuration source.
 */
public interface Configuration {

	/**
	 * Loads this configuration.
	 * <p>
	 * All fields with the {@link ConfigurationOption} annotation will be populated with values from the configuration source.
	 * 
	 * @throws IOException If an IO error occurs
	 */
	public void load() throws IOException;
}
