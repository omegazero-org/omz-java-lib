/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util;

import java.util.Properties;

/**
 * Contains utility methods for reading {@link Properties} and system properties.
 * 
 * @since 2.2
 */
public final class PropertyUtil {


	private PropertyUtil() {
	}


	/**
	 * Returns the system property value associated with the given <b>key</b>. If the property does not exist, <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The value of <b>key</b>, or <b>def</b> if the property does not exist
	 * @see #getString(Properties, String, String)
	 */
	public static String getString(String key, String def) {
		return PropertyUtil.getString(System.getProperties(), key, def);
	}

	/**
	 * Gets the system property value associated with the given <b>key</b> and attempts to parse it as an integer. If the property does not exist or the value is not a valid
	 * number, <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The numeric value of <b>key</b>, or <b>def</b> if the property is invalid
	 * @see #getInt(Properties, String, int)
	 */
	public static int getInt(String key, int def) {
		return PropertyUtil.getInt(System.getProperties(), key, def);
	}

	/**
	 * Gets the system property value associated with the given <b>key</b> and attempts to parse it as a long integer. If the property does not exist or the value is not a valid
	 * number, <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The numeric value of <b>key</b>, or <b>def</b> if the property is invalid
	 * @see #getLong(Properties, String, long)
	 */
	public static long getLong(String key, long def) {
		return PropertyUtil.getLong(System.getProperties(), key, def);
	}

	/**
	 * Gets the system property value associated with the given <b>key</b> and attempts to parse it as a floating point number. If the property does not exist or the value is not a
	 * valid number, <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The numeric value of <b>key</b>, or <b>def</b> if the property is invalid
	 * @see #getFloat(Properties, String, float)
	 */
	public static float getFloat(String key, float def) {
		return PropertyUtil.getFloat(System.getProperties(), key, def);
	}

	/**
	 * Gets the system property value associated with the given <b>key</b> and attempts to parse it as a double-precision floating point number. If the property does not exist or
	 * the value is not a valid number, <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The numeric value of <b>key</b>, or <b>def</b> if the property is invalid
	 * @see #getDouble(Properties, String, double)
	 */
	public static double getDouble(String key, double def) {
		return PropertyUtil.getDouble(System.getProperties(), key, def);
	}

	/**
	 * Gets the system property value associated with the given <b>key</b> and attempts to parse it a boolean. If the property does not exist or the value is not a valid boolean,
	 * <b>def</b> is returned.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The boolean value of <b>key</b>, or <b>def</b> if the property is invalid
	 * @see #getBoolean(Properties, String, boolean)
	 */
	public static boolean getBoolean(String key, boolean def) {
		return PropertyUtil.getBoolean(System.getProperties(), key, def);
	}


	/**
	 * Returns whether the system property identified by the given <b>key</b> is set.
	 *
	 * @param key The key
	 * @return {@code true} if a system property with the given <b>key</b> exists
	 * @since 2.8
	 */
	public static boolean isPropertySet(String key) {
		return System.getProperties().containsKey(key);
	}

	/**
	 * Sets the system property with the given <b>key</b> to the given <b>value</b>, if no property with the given <b>key</b> is already set.
	 *
	 * @param key The key
	 * @param value The value
	 * @return {@code true} if a system property with the given <b>key</b> existed already
	 * @since 2.11.0
	 */
	public static boolean setDefault(String key, String value) {
		boolean exists = isPropertySet(key);
		if(!exists)
			System.setProperty(key, value);
		return exists;
	}


	/**
	 * Returns the string associated with the given <b>key</b> in the <code>Properties</code> object. If there is no mapping for the given key, the value passed to <b>def</b>
	 * is returned.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key
	 * @return The value of <b>key</b>, or <b>def</b> if there is no value associated with the key
	 * @see Properties#getProperty(String, String)
	 */
	public static String getString(Properties props, String key, String def) {
		return props.getProperty(key, def);
	}

	/**
	 * Gets the string associated with the given <b>key</b> in the <code>Properties</code> object and attempts to parse it as an integer. If there is no mapping for the given
	 * key or the value is not a valid number, the value passed to <b>def</b> is returned.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see #getLong(Properties, String, long)
	 * @see Properties#getProperty(String)
	 * @see Integer#parseInt(String)
	 */
	public static int getInt(Properties props, String key, int def) {
		String v = props.getProperty(key);
		if(v == null)
			return def;
		try{
			return Integer.parseInt(v);
		}catch(NumberFormatException e){
			return def;
		}
	}

	/**
	 * Gets the string associated with the given <b>key</b> in the <code>Properties</code> object and attempts to parse it as a long integer. If there is no mapping for the
	 * given key or the value is not a valid number, the value passed to <b>def</b> is returned.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see #getInt(Properties, String, int)
	 * @see Properties#getProperty(String)
	 * @see Long#parseLong(String)
	 */
	public static long getLong(Properties props, String key, long def) {
		String v = props.getProperty(key);
		if(v == null)
			return def;
		try{
			return Long.parseLong(v);
		}catch(NumberFormatException e){
			return def;
		}
	}

	/**
	 * Gets the string associated with the given <b>key</b> in the <code>Properties</code> object and attempts to parse it as a floating point number. If there is no mapping
	 * for the given key or the value is not a valid number, the value passed to <b>def</b> is returned.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see #getDouble(Properties, String, double)
	 * @see Properties#getProperty(String)
	 * @see Float#parseFloat(String)
	 */
	public static float getFloat(Properties props, String key, float def) {
		String v = props.getProperty(key);
		if(v == null)
			return def;
		try{
			return Float.parseFloat(v);
		}catch(NumberFormatException e){
			return def;
		}
	}

	/**
	 * Gets the string associated with the given <b>key</b> in the <code>Properties</code> object and attempts to parse it as a double-precision floating point number. If
	 * there is no mapping for the given key or the value is not a valid number, the value passed to <b>def</b> is returned.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see #getFloat(Properties, String, float)
	 * @see Properties#getProperty(String)
	 * @see Double#parseDouble(String)
	 */
	public static double getDouble(Properties props, String key, double def) {
		String v = props.getProperty(key);
		if(v == null)
			return def;
		try{
			return Double.parseDouble(v);
		}catch(NumberFormatException e){
			return def;
		}
	}

	/**
	 * Gets the string associated with the given <b>key</b> in the <code>Properties</code> object and attempts to parse it a boolean. If there is no mapping for the given key
	 * or the value is not a valid boolean, the value passed to <b>def</b> is returned.
	 * <p>
	 * A string will be interpreted as the boolean value <code>true</code> if the string equals <code>"true"</code> or <code>"1"</code> and as <code>false</code> if the string
	 * equals <code>"false"</code> or <code>"0"</code>.
	 * 
	 * @param props The properties
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The boolean value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid boolean
	 * @see Properties#getProperty(String)
	 */
	public static boolean getBoolean(Properties props, String key, boolean def) {
		String v = props.getProperty(key);
		if("true".equals(v) || "1".equals(v))
			return true;
		else if("false".equals(v) || "0".equals(v))
			return false;
		else
			return def;
	}
}
