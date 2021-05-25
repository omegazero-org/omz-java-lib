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
package org.omegazero.common.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.omegazero.common.logging.Logger;
import org.omegazero.common.logging.LoggerUtil;

public class PluginManager {

	private static final Logger logger = LoggerUtil.createLogger();

	public static final int EXIT_ON_ERROR = 1;
	public static final int ALLOW_NONJAR = 2;
	public static final int RECURSIVE = 4;
	public static final int ALLOW_DIRS = 8;


	private final List<Plugin> plugins = new ArrayList<>();


	/**
	 * @see PluginManager#loadFromDirectory(Path, int)
	 */
	public int loadFromDirectory(String path, int flags) throws IOException {
		return this.loadFromDirectory(Paths.get(path), flags);
	}

	/**
	 * Searches for plugins in the given directory and loads them using {@link PluginManager#loadPlugin(Path)}.<br>
	 * <br>
	 * The search behavior may be configured using the <b>flags</b> parameter. This parameter is a bit field of @{@link PluginManager#EXIT_ON_ERROR},
	 * {@link PluginManager#ALLOW_NONJAR}, {@link PluginManager#RECURSIVE} or {@link PluginManager#ALLOW_DIRS}. The flags have the following behavior:
	 * <ul>
	 * <li><code>EXIT_ON_ERROR</code> - Exit this function when an error occurs while loading a plugin, rethrowing the caught exception</li>
	 * <li><code>ALLOW_NONJAR</code> - Try to load files that do not end in <code>.jar</code></li>
	 * <li><code>RECURSIVE</code> - Search for additional plugins in subdirectories</li>
	 * <li><code>ALLOW_DIRS</code> - Allow loading plugins that are not contained within a single file but within a directory. This flag is not affected by
	 * <code>ALLOW_NONJAR</code></li>
	 * </ul>
	 * <code>RECURSIVE</code> and <code>ALLOW_DIRS</code> are mutually exclusive. If both are specified, the function behaves as if only <code>RECURSIVE</code> were set.
	 * 
	 * @param path  The directory to load plugins from
	 * @param flags Bit field of flags
	 * @return The number of plugins loaded
	 * @throws IOException If an IO error occurs
	 */
	public int loadFromDirectory(Path path, int flags) throws IOException {
		int count = 0;
		Iterator<Path> paths = Files.list(path).iterator();
		Path p;
		while(paths.hasNext()){
			p = paths.next();
			logger.trace("Found ", p);
			boolean isDirectory = Files.isDirectory(p);
			if((flags & RECURSIVE) != 0 && isDirectory){
				logger.trace("Entering directory ", path);
				this.loadFromDirectory(p, flags);
			}else if(Files.isRegularFile(p) || ((flags & ALLOW_DIRS) != 0 && isDirectory)){
				if((flags & ALLOW_NONJAR) == 0 && !isDirectory && !p.toString().toLowerCase().endsWith(".jar"))
					continue;
				logger.trace("Loading plugin ", p);
				try{
					this.loadPlugin(p);
					count++;
				}catch(Exception e){
					if((flags & EXIT_ON_ERROR) != 0)
						throw e;
					else
						logger.error("Error while loading plugin at ", p, ": ", e);
				}
			}
		}
		return count;
	}

	/**
	 * Loads and initializes the plugin at <b>path</b> and registers it with this <code>PluginManager</code> instance.
	 * 
	 * @param path The path of the plugin
	 * @throws IOException If an IO error occurs
	 */
	public void loadPlugin(Path path) throws IOException {
		Plugin p = new Plugin(path);
		p.init();
		this.plugins.add(p);
	}

	/**
	 * 
	 * @return The number of plugins registered in this <code>PluginManager</code>
	 */
	public int pluginCount() {
		return this.plugins.size();
	}

	public void forEachPlugin(Consumer<Plugin> action) {
		this.plugins.forEach(action);
	}
}
