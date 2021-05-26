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

public class Plugin {

	private static final Logger logger = LoggerUtil.createLogger();

	private static final String META_FILE_NAME = "plugin.cfg";

	private final File pathFile;

	private final String path;
	private final boolean directoryPlugin;

	private ClassLoader classLoader;

	private String id;
	private String name;
	private String version;
	private String description;
	private String mainClass;
	private final Map<String, String> additionalOptions = new HashMap<String, String>();

	private Class<?> mainClassType;
	private Object mainClassInstance;

	public Plugin(String path) {
		this(new File(path));
	}

	public Plugin(Path path) {
		this(path.toFile());
	}

	public Plugin(File file) {
		this.pathFile = file;
		this.path = this.pathFile.getAbsolutePath();
		this.directoryPlugin = Files.isDirectory(Paths.get(this.path));
	}


	public void init() {
		if(this.classLoader != null)
			return;
		try{
			this.classLoader = new URLClassLoader(new URL[] { this.pathFile.toURI().toURL() }, ClassLoader.getSystemClassLoader());
			this.loadMetaFile();
			logger.debug(this.getName(), ": Loading main class ", this.mainClass);
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
			if(!value.matches("[a-zA-Z0-9\\. \\-_\\+\\(\\)$]+"))
				throw new InvalidPluginException(this.getName(), "Invalid value '" + value + "'");

			if(key.equals("id")){
				if(!value.matches("[a-zA-Z0-9]+"))
					throw new InvalidPluginException(this.getName(), "Illegal characters in 'id' option");
				this.id = value;
			}else if(key.equals("name")){
				this.name = value;
			}else if(key.equals("version")){
				this.version = value;
			}else if(key.equals("description")){
				this.description = value;
			}else if(key.equals("mainClass")){
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


	/**
	 * The plugin ID is a string given in the plugin metadata file consisting only of upper- or lowercase letters and numbers.<br>
	 * <br>
	 * If a {@link PluginManager} is used to load multiple plugins, it will ensure that an ID is unique among all plugins.
	 * 
	 * @return The unique ID of this plugin
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Returns the name of this plugin. If no name was set in the metadata file or {@link #init()} was not called, a name will be inferred from the path given in the
	 * constructor.
	 * 
	 * @return The name of this plugin
	 */
	public String getName() {
		return this.name != null ? this.name : Plugin.getFileBaseName(this.path);
	}

	/**
	 * 
	 * @return The version string set in the metadata file or <code>null</code> if none was set
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * 
	 * @return A description provided in the metadata file or <code>null</code> if none was provided
	 */
	public String getDescription() {
		return this.description;
	}

	public String getAdditionalOption(String key) {
		return this.additionalOptions.get(key);
	}


	public Class<?> getMainClassType() {
		return this.mainClassType;
	}

	public Object getMainClassInstance() {
		return this.mainClassInstance;
	}


	private static String getFileBaseName(String path) {
		return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
	}
}
