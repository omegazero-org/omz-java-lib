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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A read-only map of key-value pairs, where the value is any type of data identified by the key string, providing a layer of abstraction for different data representations.
 * Created by a {@link Configuration} instance.
 * 
 * @since 2.4
 */
public class ConfigObject implements Serializable {

	private static final long serialVersionUID = 1L;


	protected final Map<String, Object> data;

	public ConfigObject() {
		this(new HashMap<>());
	}

	public ConfigObject(Map<String, Object> data) {
		if(data == null)
			throw new NullPointerException("data is null");
		this.data = data;
	}


	/**
	 * 
	 * @return The number of key-value pairs in this <code>ConfigObject</code>
	 * @see Map#size()
	 */
	public int size() {
		return this.data.size();
	}

	/**
	 * 
	 * @return <code>true</code> if this <code>ConfigObject</code> contains no key-value pairs
	 * @see Map#isEmpty()
	 */
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	/**
	 * 
	 * @param value The key to search for
	 * @return <code>true</code> if this <code>ConfigObject</code> contains the given <b>key</b>
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		return this.data.containsKey(key);
	}

	/**
	 * 
	 * @param value The value to search for
	 * @return <code>true</code> if this <code>ConfigObject</code> contains the given <b>value</b>
	 * @see Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return this.data.containsValue(value);
	}

	/**
	 * 
	 * @return An unmodifiable set of all keys of this <code>ConfigObject</code>
	 */
	public Set<String> keySet() {
		return Collections.unmodifiableSet(this.data.keySet());
	}

	/**
	 * 
	 * @return An unmodifiable collection of all values of this <code>ConfigObject</code>
	 */
	public Collection<Object> values() {
		return Collections.unmodifiableCollection(this.data.values());
	}

	/**
	 * 
	 * @return An unmodifiable set of all entries in this <code>ConfigObject</code>
	 */
	public Set<Map.Entry<String, Object>> entrySet() {
		return Collections.unmodifiableSet(this.data.entrySet());
	}

	/**
	 * Creates a new {@link HashMap} with all values of this <code>ConfigObject</code>. Changes to the returned map have no effect on this <code>ConfigObject</code>.
	 * 
	 * @return A <code>Map</code> with all values of this <code>ConfigObject</code>
	 */
	public Map<String, Object> copyData() {
		return new HashMap<>(this.data);
	}


	/**
	 * Creates a new <code>ConfigObject</code> that contains all properties of this and the given <code>ConfigObject</code>. If both have a property with the same key, the
	 * value of the property in the given <code>ConfigObject</code> is put into the new <code>ConfigObject</code>.<br>
	 * Both provided objects stay unchanged.
	 * 
	 * @param other The <code>ConfigObject</code> to merge this one with, possibly overriding values of this <code>ConfigObject</code>
	 * @return A new <code>ConfigObject</code> with values merged from this and <b>other</b>
	 */
	public ConfigObject merge(ConfigObject other) {
		Map<String, Object> newData = new HashMap<>(this.data.size() + other.data.size());
		newData.putAll(this.data);
		newData.putAll(other.data);
		return new ConfigObject(newData);
	}


	/**
	 * Returns a value associated with the given <b>key</b> in this <code>ConfigObject</code>, or <code>null</code> if the given key has no value set. Note that a return value
	 * of <code>null</code> may also indicate that the given key is explicitly mapped to <code>null</code>.<br>
	 * <br>
	 * This method can be used to get a value of any type. To eliminate the need for type checking in the caller code, any of the <code>get*</code> and <code>opt*</code>
	 * methods may be used instead. The name and return value of those methods indicates the type that is checked for. Corresponding methods in both groups are equivalent,
	 * except that the methods starting with <code>get</code> throw an <code>IllegalArgumentException</code> if the value does not exist or is not the expected type, while the
	 * methods starting with <code>opt</code> return <code>null</code> or the value passed to the <b>def</b> argument instead.
	 * 
	 * @param key The key
	 * @return The value mapped to the given <b>key</b> or <code>null</code> if no such value exists
	 */
	public Object get(String key) {
		return this.data.get(key);
	}


	public ConfigObject getObject(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigObject)
			return (ConfigObject) v;
		else
			throw new IllegalArgumentException("Expected object for '" + key + "' but received type " + getTypeName(v));
	}

	public ConfigArray getArray(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigArray)
			return (ConfigArray) v;
		else
			throw new IllegalArgumentException("Expected array for '" + key + "' but received type " + getTypeName(v));
	}

	public String getString(String key) {
		Object v = this.get(key);
		if(v instanceof String)
			return (String) v;
		else
			throw new IllegalArgumentException("Expected string for '" + key + "' but received type " + getTypeName(v));
	}

	public int getInt(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).intValue();
		else
			throw new IllegalArgumentException("Expected integer for '" + key + "' but received type " + getTypeName(v));
	}

	public long getLong(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).longValue();
		else
			throw new IllegalArgumentException("Expected integer for '" + key + "' but received type " + getTypeName(v));
	}

	public float getFloat(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).floatValue();
		else
			throw new IllegalArgumentException("Expected floating point value for '" + key + "' but received type " + getTypeName(v));
	}

	public double getDouble(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).doubleValue();
		else
			throw new IllegalArgumentException("Expected floating point value for '" + key + "' but received type " + getTypeName(v));
	}

	public boolean getBoolean(String key) {
		Object v = this.get(key);
		if(v instanceof Boolean)
			return (boolean) v;
		else
			throw new IllegalArgumentException("Expected boolean for '" + key + "' but received type " + getTypeName(v));
	}


	public ConfigObject optObject(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigObject)
			return (ConfigObject) v;
		else
			return null;
	}

	public ConfigArray optArray(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigArray)
			return (ConfigArray) v;
		else
			return null;
	}

	public String optString(String key, String def) {
		Object v = this.get(key);
		if(v instanceof String)
			return (String) v;
		else
			return def;
	}

	public int optInt(String key, int def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).intValue();
		else
			return def;
	}

	public long optLong(String key, long def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).longValue();
		else
			return def;
	}

	public float optFloat(String key, float def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).floatValue();
		else
			return def;
	}

	public double optDouble(String key, double def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).doubleValue();
		else
			return def;
	}

	public boolean optBoolean(String key, boolean def) {
		Object v = this.get(key);
		if(v instanceof Boolean)
			return (boolean) v;
		else
			return def;
	}


	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ConfigObject))
			return false;
		return ((ConfigObject) o).data.equals(this.data);
	}

	@Override
	public ConfigObject clone() {
		return new ConfigObject(this.copyData());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for(Map.Entry<String, Object> e : this.data.entrySet()){
			sb.append('"').append(e.getKey()).append("\" = ").append(e.getValue()).append(", ");
		}
		sb.replace(sb.length() - 2, sb.length(), " }");
		return sb.toString();
	}


	private static String getTypeName(Object obj) {
		return obj == null ? "null" : obj.getClass().getName();
	}
}
