package org.omegazero.common.util;

import java.util.Properties;

public final class PropertyUtil {


	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getString(Properties, String, String)} with the <code>Properties</code> object returned by
	 *         {@link System#getProperties()}
	 */
	public static String getString(String key, String def) {
		return PropertyUtil.getString(System.getProperties(), key, def);
	}

	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getInt(Properties, String, int)} with the <code>Properties</code> object returned by {@link System#getProperties()}
	 */
	public static int getInt(String key, int def) {
		return PropertyUtil.getInt(System.getProperties(), key, def);
	}

	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getLong(Properties, String, long)} with the <code>Properties</code> object returned by {@link System#getProperties()}
	 */
	public static long getLong(String key, long def) {
		return PropertyUtil.getLong(System.getProperties(), key, def);
	}

	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getFloat(Properties, String, float)} with the <code>Properties</code> object returned by
	 *         {@link System#getProperties()}
	 */
	public static float getFloat(String key, float def) {
		return PropertyUtil.getFloat(System.getProperties(), key, def);
	}

	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getDouble(Properties, String, double)} with the <code>Properties</code> object returned by
	 *         {@link System#getProperties()}
	 */
	public static double getDouble(String key, double def) {
		return PropertyUtil.getDouble(System.getProperties(), key, def);
	}

	/**
	 * 
	 * @return The value returned by {@link PropertyUtil#getBoolean(Properties, String, boolean)} with the <code>Properties</code> object returned by
	 *         {@link System#getProperties()}
	 */
	public static boolean getBoolean(String key, boolean def) {
		return PropertyUtil.getBoolean(System.getProperties(), key, def);
	}


	/**
	 * Returns the string associated with the given <b>key</b> in the <code>Properties</code> object. If there is no mapping for the given key, the value passed to <b>def</b>
	 * is returned.
	 * 
	 * @param props
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
	 * @param props
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see Properties#getProperty(String)
	 * @see Integer#parseInt(String)
	 * @see PropertyUtil#getLong(Properties, String, long)
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
	 * @param props
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see Properties#getProperty(String)
	 * @see Long#parseLong(String)
	 * @see PropertyUtil#getInt(Properties, String, int)
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
	 * @param props
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see Properties#getProperty(String)
	 * @see Float#parseFloat(String)
	 * @see PropertyUtil#getDouble(Properties, String, double)
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
	 * @param props
	 * @param key   The key
	 * @param def   The default value to return if there is no value for the key or the value is not valid
	 * @return The numeric value of <b>key</b>, or <b>def</b> if there is no value associated with the key or the value is not a valid number
	 * @see Properties#getProperty(String)
	 * @see Double#parseDouble(String)
	 * @see PropertyUtil#getFloat(Properties, String, float)
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
	 * or the value is not a valid boolean, the value passed to <b>def</b> is returned. <br>
	 * <br>
	 * A string will be interpreted as the boolean value <code>true</code> if the string equals <code>"true"</code> or <code>"1"</code> and as <code>false</code> if the string
	 * equals <code>"false"</code> or <code>"0"</code>.
	 * 
	 * @param props
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
