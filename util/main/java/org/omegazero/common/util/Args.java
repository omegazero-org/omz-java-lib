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
package org.omegazero.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used for parsing command line arguments.
 * 
 * @since 2.1
 */
public abstract class Args implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The argument data.
	 */
	protected final Map<String, Object> arguments = new HashMap<>();


	/**
	 * Parses the given argument array and stores the data in this {@link Args} object.
	 * <p>
	 * Values identified as integers, booleans ("<code>true</code>" and "<code>false</code>") or floating-point numbers are automatically converted to the respective type.
	 * 
	 * @param args The argument array
	 * @implNote Use {@link #put(String, String)} to store values
	 */
	public abstract void parseArguments(String[] args);


	/**
	 * Returns the string value identified by the given <b>key</b>, or <code>null</code> if no value with the specified key exists.
	 * 
	 * @param key The key string
	 * @return The value or <code>null</code> if it does not exist
	 */
	public String getValue(String key) {
		Object v = this.arguments.get(key);
		if(v != null)
			return v.toString();
		else
			return null;
	}

	/**
	 * Returns the string value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public String getValueOrDefault(String key, String def) {
		Object val = this.arguments.get(key);
		if(val != null)
			return val.toString();
		else
			return def;
	}

	/**
	 * Returns the integer value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists or is not a number.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public int getIntOrDefault(String key, int def) {
		return (int) this.getLongOrDefault(key, def);
	}

	/**
	 * Returns the long integer value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists or is not a number.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public long getLongOrDefault(String key, long def) {
		Object val = this.arguments.get(key);
		if(val instanceof Number)
			return ((Number) val).longValue();
		else
			return def;
	}

	/**
	 * Returns the single-precision floating-point number value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists or is not a number.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public float getFloatOrDefault(String key, float def) {
		return (float) this.getDoubleOrDefault(key, def);
	}

	/**
	 * Returns the double-precision floating-point number value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists or is not a number.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public double getDoubleOrDefault(String key, double def) {
		Object val = this.arguments.get(key);
		if(val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return def;
	}

	/**
	 * Returns the boolean value identified by the given <b>key</b>, or <b>def</b> if no value with the specified key exists or is not a boolean.
	 * 
	 * @param key The key string
	 * @param def The default value to return if no value with the given <b>key</b> exists
	 * @return The value or <b>def</b> if it does not exist
	 */
	public boolean getBooleanOrDefault(String key, boolean def) {
		Object val = this.arguments.get(key);
		if(val instanceof Boolean)
			return (boolean) val;
		else
			return def;
	}


	/**
	 * Stores the given <b>value</b> identified by the <b>key</b> in this object. The <b>value</b> is automatically converted to the appropriate type, if applicable (for example,
	 * strings consisting only of digits are converted to a number).
	 * 
	 * @param key The key
	 * @param value The value
	 */
	protected void put(String key, String value) {
		if(value.matches("-?\\d+")){
			this.arguments.put(key, Long.parseLong(value));
		}else if(value.matches("-?((\\d+)|(\\d*\\.\\d+)|(\\d+\\.\\d*))((e|E)-?\\d+)?")){
			this.arguments.put(key, Double.parseDouble(value));
		}else if(value.equals("true")){
			this.arguments.put(key, true);
		}else if(value.equals("false")){
			this.arguments.put(key, false);
		}else{
			this.arguments.put(key, value);
		}
	}


	/**
	 * Parses the given array of arguments using the default format. See {@link DefaultFormat#parseArguments(String[])}.
	 * 
	 * @param args The argument array
	 * @return The {@link Args} object representing the parsed data
	 */
	public static Args parse(String[] args) {
		Args a = new DefaultFormat();
		a.parseArguments(args);
		return a;
	}


	/**
	 * An {@link Args} implementation for the default argument format.
	 * 
	 * @since 2.8
	 */
	public static class DefaultFormat extends Args {

		private static final long serialVersionUID = 1L;


		/**
		 * {@inheritDoc}
		 * <p>
		 * Arguments are parsed as key-value pairs. A key-value pair consists of a pair of elements in the given array: the first element is <code>--</code> followed by the key
		 * string, the second element is the value string. For example:
		 * 
		 * <pre>
		 * --exampleKey value
		 * </pre>
		 * 
		 * The key is "<code>exampleKey</code>" and the value is "<code>value</code>". If a key string such as "<code>--key</code>" has no value (at the end of the array), it is
		 * ignored.
		 * <p>
		 * To set a key (for example "<code>key</code>") to the boolean value <code>true</code>, the shorthand "<code>-key</code>" (single dash) may be used.
		 */
		@Override
		public void parseArguments(String[] args) {
			for(int i = 0; i < args.length; i++){
				String arg = args[i];
				if(arg.startsWith("--") && i + 1 < args.length){
					String key = arg.substring(2);
					this.put(key, args[i + 1]);
					i++;
				}else if(arg.startsWith("-")){
					String key = arg.substring(1);
					this.arguments.put(key, true);
				}
			}
		}
	}
}
