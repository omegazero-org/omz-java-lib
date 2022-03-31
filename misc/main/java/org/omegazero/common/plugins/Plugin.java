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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.omegazero.common.logging.Logger;
import org.omegazero.common.logging.LoggerUtil;

/**
 * Represents a plugin managed by a {@link PluginManager}.
 * <p>
 * Plugins are JAR files or directories containing class files. A plugin must have a file called {@code plugin.cfg} in its root directory, containing metadata about the plugin.
 * <p>
 * The metadata file is a list of key-value pairs. Each pair is separated by one or more newline characters. The key must consist only of alphanumeric characters, followed by a
 * {@code =}, and the value, which has different character restrictions depending on the key. The following keys are defined:
 * <table>
 * <tr><th>key</th><th>Meaning</th><th>Required</th><th>Allowed characters</th></tr>
 * <tr><td>id</td><td>The unique ID of the plugin</td><td>yes</td><td>alphanumeric, {@code -}, {@code _}</td></tr>
 * <tr><td>name</td><td>The name of the plugin</td><td>no</td><td>alphanumeric, various special characters</td></tr>
 * <tr><td>version</td><td>A version string</td><td>no</td><td>alphanumeric, {@code -}, {@code _}, {@code .}, {@code +}</td></tr>
 * <tr><td>description</td><td>A description</td><td>no</td><td>any</td></tr>
 * <tr><td>mainClass</td><td>The main class of the plugin</td><td>yes</td><td>Java identifier characters</td></tr>
 * </table>
 * Other keys not listed in this table are stored and may be retrieved using {@link #getAdditionalOption(String)}.
 * 
 * @since 2.2
 */
public class Plugin {

	private static final Logger logger = LoggerUtil.createLogger();

	private static final String META_FILE_NAME = "plugin.cfg";

	private final PluginClassLoader classLoader;
	private final File pathFile;

	private final String path;
	private final boolean directoryPlugin;

	private boolean init = false;

	private String id;
	private String name;
	private String version;
	private String description;
	private String mainClass;
	private final Map<String, String> additionalOptions = new HashMap<String, String>();

	private Class<?> mainClassType;
	private Object mainClassInstance;

	public Plugin(Path path, PluginClassLoader classLoader) {
		this(path.toFile(), classLoader);
	}

	public Plugin(File file, PluginClassLoader classLoader) {
		this.pathFile = file;
		this.classLoader = classLoader;
		this.path = this.pathFile.getAbsolutePath();
		this.directoryPlugin = Files.isDirectory(Paths.get(this.path));
	}


	/**
	 * Loads the metadata file and main class of this {@code Plugin}.
	 */
	public void init() {
		if(this.init)
			return;
		this.init = true;
		try{
			this.classLoader.addURL(this.pathFile.toURI().toURL());
			this.loadMetaFile();
			logger.debug("Loading main class of '", this.getName(), "': ", this.mainClass);
			this.mainClassType = Class.forName(this.mainClass, true, this.classLoader);
			this.mainClassInstance = this.mainClassType.newInstance();
		}catch(IOException | ReflectiveOperationException e){
			throw new RuntimeException("Error while loading plugin at '" + this.path + "'", e);
		}
	}

	private InputStream tryLoadMetaFile(String name) {
		try{
			String file;
			if(this.directoryPlugin)
				file = "file:" + this.path + "/" + name;
			else
				file = "jar:file:" + this.path + "!/" + name;
			return new URL(file).openConnection().getInputStream();
		}catch(IOException e){
			return null;
		}
	}

	private void loadMetaFile() throws IOException {
		String filename = Plugin.getFileBaseName(this.path);

		InputStream fileIs = this.tryLoadMetaFile(Plugin.META_FILE_NAME);
		if(fileIs == null){
			int e = filename.lastIndexOf('.');
			fileIs = this.tryLoadMetaFile((e > 0 ? filename.substring(0, e) : filename) + ".cfg");
		}
		if(fileIs == null)
			throw new InvalidPluginException(this.getName(), "Missing metadata file");

		byte[] bdata = new byte[fileIs.available()];
		fileIs.read(bdata, 0, bdata.length);
		String[] lines = new String(bdata).replace('\r', '\n').split("\n");
		for(String l : lines){
			l = l.trim();
			if(l.length() < 1 || l.charAt(0) == '#' /* <- comment */)
				continue;
			String[] p = l.split("=", 2);
			if(p.length < 2)
				continue;

			String key = p[0].trim();
			String value = p[1].trim();
			if(!key.matches("[a-zA-Z0-9]+"))
				throw new InvalidPluginException(this.getName(), "Invalid key '" + key + "'");

			if(key.equals("id")){
				this.validateValue(key, value, "[a-zA-Z0-9_\\-]+");
				this.id = value;
			}else if(key.equals("name")){
				this.validateValue(key, value, "[a-zA-Z0-9\\. \\-_\\+\\(\\)$]+");
				this.name = value;
			}else if(key.equals("version")){
				this.validateValue(key, value, "[a-zA-Z0-9\\.\\-_\\+]+");
				this.version = value;
			}else if(key.equals("description")){
				this.description = value;
			}else if(key.equals("mainClass")){
				this.validateValue(key, value, "[a-zA-Z0-9\\._$]+");
				this.mainClass = value;
			}else{
				this.additionalOptions.put(key, value);
			}
		}

		if(this.id == null)
			throw new InvalidPluginException(this.getName(), "Missing required 'id' option in meta file");
		if(this.name == null)
			this.name = filename;
		if(this.mainClass == null)
			throw new InvalidPluginException(this.getName(), "Missing required 'mainClass' option in meta file");
	}

	private void validateValue(String key, String value, String regex) {
		if(!value.matches(regex))
			throw new InvalidPluginException(this.getName(), "Illegal characters in '" + key + "' option");
	}


	/**
	 * The plugin ID is a string given in the plugin metadata file consisting only of upper- or lowercase letters and numbers.
	 * <p>
	 * If a {@link PluginManager} is used to load multiple plugins, it will ensure that an ID is unique among all plugins.
	 * 
	 * @return The unique ID of this plugin
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the name of this plugin. If no name was set in the metadata file or {@link #init()} was not called, a name will be inferred from the path given in the constructor.
	 * 
	 * @return The name of this plugin
	 */
	public String getName() {
		return this.name != null ? this.name : Plugin.getFileBaseName(this.path);
	}

	/**
	 * Returns the version string set in the metadata file or <code>null</code> if none was set.
	 * 
	 * @return The version string
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Returns a description provided in the metadata file or <code>null</code> if none was provided.
	 * 
	 * @return The description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the value of an unrecognized option in the metadata file.
	 * 
	 * @param key The key
	 * @return The value
	 */
	public String getAdditionalOption(String key) {
		return this.additionalOptions.get(key);
	}


	/**
	 * Returns the type of the main class of this {@code Plugin}, loaded by the class loader passed in the constructor.
	 * 
	 * @return The type
	 * @throws IllegalStateException If {@link #init()} was not called successfully prior to calling this method
	 */
	public Class<?> getMainClassType() {
		if(this.mainClassType == null)
			throw new IllegalStateException("Plugin is not initialized");
		return this.mainClassType;
	}

	/**
	 * Returns the instance of the main class of this {@code Plugin}.
	 * 
	 * @return The instance
	 * @throws IllegalStateException If {@link #init()} was not called successfully prior to calling this method
	 */
	public Object getMainClassInstance() {
		if(this.mainClassInstance == null)
			throw new IllegalStateException("Plugin is not initialized");
		return this.mainClassInstance;
	}


	private static String getFileBaseName(String path) {
		return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
	}


	static class PluginClassLoader extends URLClassLoader {

		public PluginClassLoader() {
			super(new URL[0], ClassLoader.getSystemClassLoader());
		}

		@Override
		public void addURL(URL url) {
			super.addURL(url);
		}
	}
}
