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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.omegazero.common.logging.Logger;
import org.omegazero.common.logging.LoggerUtil;

/**
 * Manages and loads {@link Plugin}s.
 * 
 * @since 2.2
 */
public class PluginManager implements Iterable<Plugin> {

	private static final Logger logger = LoggerUtil.createLogger();

	/**
	 * Exit the method when an error occurs while loading a plugin, rethrowing the caught exception.
	 */
	public static final int EXIT_ON_ERROR = 1;
	/**
	 * Try to load any files, not only files ending with {@code .jar}.
	 */
	public static final int ALLOW_NONJAR = 2;
	/**
	 * Search for additional plugins in subdirectories.
	 */
	public static final int RECURSIVE = 4;
	/**
	 * Allow loading plugins that are not contained within a single file but within a directory. This flag is not affected by {@code ALLOW_NONJAR}.
	 */
	public static final int ALLOW_DIRS = 8;


	private final PluginClassLoader classLoader = new PluginClassLoader();
	private final List<Plugin> plugins = new ArrayList<>();


	/**
	 * Loads plugins from the given directory. See {@link #loadFromDirectory(Path, int)}.
	 * 
	 * @param path The directory to load plugins from
	 * @param flags Bit field of flags
	 * @return The number of plugins loaded
	 * @throws IOException If an IO error occurs
	 */
	public int loadFromDirectory(String path, int flags) throws IOException {
		return this.loadFromDirectory(Paths.get(path), flags);
	}

	/**
	 * Searches for plugins in the given directory and loads them using {@link #loadPlugin(Path)}.
	 * <p>
	 * The search behavior may be configured using the <b>flags</b> parameter. This parameter is a bit field of {@link #EXIT_ON_ERROR}, {@link #ALLOW_NONJAR}, {@link #RECURSIVE}
	 * and {@link #ALLOW_DIRS}. {@code RECURSIVE} and {@code ALLOW_DIRS} are mutually exclusive. If both are specified, the function behaves as if only {@code RECURSIVE} were set.
	 * 
	 * @param path The directory to load plugins from
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
	 * Searches for plugins in the JAR file at the given path, by attempting to load any nested JAR files, whose relative path in the given JAR file start with <b>innerPath</b>.
	 * <p>
	 * The search behavior may be configured using the <b>flags</b> parameter. This parameter is a bit field of {@link #EXIT_ON_ERROR} and {@link #ALLOW_NONJAR}.
	 * 
	 * @param jarFilePath The path of the wrapping JAR file
	 * @param innerPath The relative path prefix in the JAR file
	 * @param flags Bit field of flags
	 * @return The number of plugins loaded
	 * @throws IOException If an IO error occurs
	 * @since 2.8
	 */
	public int loadFromJar(Path jarFilePath, String innerPath, int flags) throws IOException {
		int count = 0;
		if(!innerPath.endsWith("/"))
			innerPath += "/";
		try(JarFile jar = new JarFile(jarFilePath.toFile())){
			java.util.Enumeration<JarEntry> jarEntries = jar.entries();
			while(jarEntries.hasMoreElements()){
				JarEntry entry = jarEntries.nextElement();
				String entryName = entry.getName();
				if(entry.isDirectory() || !entryName.startsWith(innerPath) || (((flags & ALLOW_NONJAR) == 0) && !entryName.toLowerCase().endsWith(".jar")))
					continue;
				String path = jarFilePath + "/" + entryName;
				logger.trace("Loading nested JAR plugin ", path);
				try{
					this.initPlugin(new Plugin.JarInputStreamPlugin(path, this.classLoader, new java.util.jar.JarInputStream(jar.getInputStream(entry))));
					count++;
				}catch(Exception e){
					if((flags & EXIT_ON_ERROR) != 0)
						throw e;
					else
						logger.error("Error while loading plugin at ", path, ": ", e);
				}
			}
		}
		return count;
	}

	/**
	 * Searches for plugins at the given path.
	 * <p>
	 * If the given <b>path</b> contains the separator {@code !/}, the path is assumed to point inside a JAR file, with the format
	 * {@code <path to JAR file>!/<directory inside JAR file>}, and {@link #loadFromJar(Path, String, int)} is called. Otherwise, the <b>path</b> is passed unchanged to
	 * {@link #loadFromDirectory(String, int)}.
	 * 
	 * @param path The path
	 * @param flags Bit field of flags
	 * @return The number of plugins loaded
	 * @throws IOException If an IO error occurs
	 * @since 2.8
	 */
	public int loadFromPath(String path, int flags) throws IOException {
		int si = path.indexOf("!/");
		if(si >= 0){
			return this.loadFromJar(Paths.get(path.substring(0, si)), path.substring(si + 2), flags);
		}else{
			return this.loadFromDirectory(path, flags);
		}
	}


	/**
	 * Loads and initializes the plugin at <b>path</b> and registers it with this <code>PluginManager</code> instance.
	 * <p>
	 * If the given <b>path</b> is a directory, the plugin is loaded as a directory plugin, otherwise, it is loaded as a JAR plugin.
	 * 
	 * @param path The path of the plugin
	 * @throws IOException If an IO error occurs
	 */
	public void loadPlugin(Path path) throws IOException {
		if(Files.isDirectory(path))
			this.initPlugin(new Plugin.DirectoryPlugin(path, this.classLoader));
		else
			this.initPlugin(new Plugin.JarPlugin(path.toString(), this.classLoader));
	}

	private void initPlugin(Plugin plugin) {
		plugin.init();
		for(Plugin e : this.plugins){
			if(e.getId().equals(plugin.getId()))
				throw new InvalidPluginException(e.getName(), "A plugin with id '" + plugin.getId() + "' already exists");
		}
		this.plugins.add(plugin);
	}

	/**
	 * Returns the number of plugins registered in this <code>PluginManager</code>.
	 * 
	 * @return The number of plugins
	 */
	public int pluginCount() {
		return this.plugins.size();
	}


	@Override
	public Iterator<Plugin> iterator() {
		return new Iterator<Plugin>(){

			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < PluginManager.this.plugins.size();
			}

			@Override
			public Plugin next() {
				return PluginManager.this.plugins.get(this.index++);
			}
		};
	}
}
