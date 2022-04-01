/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.plugins;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

/**
 * The class loader for {@link Plugin}s.
 * 
 * @since 2.8
 */
public class PluginClassLoader extends SecureClassLoader {


	private final List<Plugin> plugins = new java.util.ArrayList<Plugin>();


	/**
	 * Adds the given plugin to the search path.
	 * 
	 * @param plugin The plugin
	 */
	public void addPlugin(Plugin plugin) {
		this.plugins.add(plugin);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		int pei = name.lastIndexOf('.');
		String packageName = pei > 0 ? name.substring(0, pei) : null;

		String path = name.replace('.', '/') + ".class";
		for(Plugin plugin : this.plugins){
			Plugin.LoadedClassFile file;
			try{
				file = plugin.loadClass(path);
				if(file == null)
					continue;
			}catch(IOException e){
				throw new ClassNotFoundException(name, e);
			}
			if(packageName != null && super.getPackage(packageName) == null){
				if(file.manifest != null){
					Attributes attributes = file.manifest.getMainAttributes();
					super.definePackage(packageName, attributes.getValue(Name.SPECIFICATION_TITLE), attributes.getValue(Name.SPECIFICATION_VERSION),
							attributes.getValue(Name.SPECIFICATION_VENDOR), attributes.getValue(Name.IMPLEMENTATION_TITLE), attributes.getValue(Name.IMPLEMENTATION_VERSION),
							attributes.getValue(Name.IMPLEMENTATION_VENDOR), null);
				}else{
					super.definePackage(packageName, null, null, null, null, null, null, null);
				}
			}
			return super.defineClass(name, file.data, 0, file.data.length, file.codeSource);
		}
		throw new ClassNotFoundException(name);
	}
}
