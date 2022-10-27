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
import java.util.List;
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


	/**
	 * The data.
	 */
	protected final Map<String, Object> data;

	/**
	 * Creates an empty {@code ConfigObject}.
	 */
	public ConfigObject() {
		this(new HashMap<>(), false);
	}

	/**
	 * Creates a {@code ConfigObject} with the given data.
	 * <p>
	 * Changes on the given map have no effects on this {@code ConfigObject}.
	 * 
	 * @param data The data
	 */
	public ConfigObject(Map<String, Object> data) {
		this(data, true);
	}

	private ConfigObject(Map<String, Object> data, boolean copy) {
		if(data == null)
			throw new NullPointerException("data is null");
		this.data = copy ? new HashMap<>(data) : data;
	}


	/**
	 * Returns the number of key-value pairs in this {@code ConfigObject}.
	 * 
	 * @return The number of key-value pairs
	 * @see Map#size()
	 */
	public int size() {
		return this.data.size();
	}

	/**
	 * Returns {@code true} if this {@code ConfigObject} contains no key-value pairs.
	 * 
	 * @return {@code true} if empty
	 * @see Map#isEmpty()
	 */
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	/**
	 * Returns whether the given <b>key</b> exists in this {@code ConfigObject}.
	 * 
	 * @param key The key to search for
	 * @return {@code true} if the <b>key</b> exists
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		return this.data.containsKey(key);
	}

	/**
	 * Returns whether the given <b>value</b> exists in this {@code ConfigObject}.
	 * 
	 * @param value The value to search for
	 * @return {@code true} if the <b>value</b> exists
	 * @see Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return this.data.containsValue(value);
	}

	/**
	 * Returns an unmodifiable set of all keys of this {@code ConfigObject}.
	 * 
	 * @return A set of all keys
	 */
	public Set<String> keySet() {
		return Collections.unmodifiableSet(this.data.keySet());
	}

	/**
	 * Returns an unmodifiable collection of all values of this {@code ConfigObject}.
	 * 
	 * @return A collection of all values
	 */
	public Collection<Object> values() {
		return Collections.unmodifiableCollection(this.data.values());
	}

	/**
	 * Returns an unmodifiable set of all entries in this {@code ConfigObject}.
	 * 
	 * @return A set of entries
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
	 * Creates a new <code>ConfigObject</code> that contains all properties of this and the given <code>ConfigObject</code>. If both have a property with the same key, the value of
	 * the property in the given <code>ConfigObject</code> is put into the new <code>ConfigObject</code>.<br>
	 * Both provided objects stay unchanged.
	 * 
	 * @param other The <code>ConfigObject</code> to merge this one with, possibly overriding values of this <code>ConfigObject</code>
	 * @return A new <code>ConfigObject</code> with values merged from this and <b>other</b>
	 */
	public ConfigObject merge(ConfigObject other) {
		Map<String, Object> newData = new HashMap<>(this.data.size() + other.data.size());
		newData.putAll(this.data);
		newData.putAll(other.data);
		return new ConfigObject(newData, false);
	}


	/**
	 * Returns a value associated with the given <b>key</b> in this <code>ConfigObject</code>, or <code>null</code> if the given key has no value set. Note that a return value of
	 * <code>null</code> may also indicate that the given key is explicitly mapped to <code>null</code>.
	 * <p>
	 * This method can be used to get a value of any type. To eliminate the need for type checking in the caller code, any of the <code>get*</code> and <code>opt*</code> methods
	 * may be used instead. The name and return value of those methods indicates the type that is checked for. Corresponding methods in both groups are equivalent, except that the
	 * methods starting with <code>get</code> throw an <code>IllegalArgumentException</code> if the value does not exist or is not the expected type, while the methods starting
	 * with <code>opt</code> return <code>null</code> or the value passed to the <b>def</b> argument instead.
	 * 
	 * @param key The key
	 * @return The value mapped to the given <b>key</b> or <code>null</code> if no such value exists
	 */
	public Object get(String key) {
		return this.data.get(key);
	}


	/**
	 * Returns a {@code ConfigObject} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code ConfigObject}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code ConfigObject}
	 */
	public ConfigObject getObject(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigObject)
			return (ConfigObject) v;
		else
			throw new IllegalArgumentException("Expected object for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code ConfigArray} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code ConfigArray}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code ConfigArray}
	 */
	public ConfigArray getArray(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigArray)
			return (ConfigArray) v;
		else
			throw new IllegalArgumentException("Expected array for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code String} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code String}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code String}
	 */
	public String getString(String key) {
		Object v = this.get(key);
		if(v instanceof String)
			return (String) v;
		else
			throw new IllegalArgumentException("Expected string for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns an {@code int} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code int}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not an {@code int}
	 */
	public int getInt(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).intValue();
		else
			throw new IllegalArgumentException("Expected integer for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code long} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code long}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code long}
	 */
	public long getLong(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).longValue();
		else
			throw new IllegalArgumentException("Expected integer for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code float} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code float}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code float}
	 */
	public float getFloat(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).floatValue();
		else
			throw new IllegalArgumentException("Expected floating point value for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code double} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code double}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code double}
	 */
	public double getDouble(String key) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).doubleValue();
		else
			throw new IllegalArgumentException("Expected floating point value for '" + key + "' but received type " + getTypeName(v));
	}

	/**
	 * Returns a {@code boolean} associated with the given <b>key</b> in this {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code boolean}
	 * @throws IllegalArgumentException If no mapping with the given key exists or is not a {@code boolean}
	 */
	public boolean getBoolean(String key) {
		Object v = this.get(key);
		if(v instanceof Boolean)
			return (boolean) v;
		else
			throw new IllegalArgumentException("Expected boolean for '" + key + "' but received type " + getTypeName(v));
	}


	/**
	 * Returns a {@code ConfigObject} associated with the given <b>key</b> in this {@code ConfigObject}, or {@code null} if no mapping the the given <b>key</b> exists or is not a
	 * {@code ConfigObject}.
	 * 
	 * @param key The key
	 * @return The {@code ConfigObject}, or {@code null} if it does not exist
	 */
	public ConfigObject optObject(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigObject)
			return (ConfigObject) v;
		else
			return null;
	}

	/**
	 * Returns a {@code ConfigArray} associated with the given <b>key</b> in this {@code ConfigObject}, or {@code null} if no mapping the the given <b>key</b> exists or is not a
	 * {@code ConfigArray}.
	 * 
	 * @param key The key
	 * @return The {@code ConfigArray}, or {@code null} if it does not exist
	 */
	public ConfigArray optArray(String key) {
		Object v = this.get(key);
		if(v instanceof ConfigArray)
			return (ConfigArray) v;
		else
			return null;
	}

	/**
	 * Returns a {@code String} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not a
	 * {@code String}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code String}
	 */
	public String optString(String key, String def) {
		Object v = this.get(key);
		if(v instanceof String)
			return (String) v;
		else
			return def;
	}

	/**
	 * Returns an {@code int} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not an
	 * {@code int}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code int}
	 */
	public int optInt(String key, int def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).intValue();
		else
			return def;
	}

	/**
	 * Returns a {@code long} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not a
	 * {@code long}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code long}
	 */
	public long optLong(String key, long def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).longValue();
		else
			return def;
	}

	/**
	 * Returns a {@code float} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not a
	 * {@code float}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code float}
	 */
	public float optFloat(String key, float def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).floatValue();
		else
			return def;
	}

	/**
	 * Returns a {@code double} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not a
	 * {@code double}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code double}
	 */
	public double optDouble(String key, double def) {
		Object v = this.get(key);
		if(v instanceof Number)
			return ((Number) v).doubleValue();
		else
			return def;
	}

	/**
	 * Returns a {@code boolean} associated with the given <b>key</b> in this {@code ConfigObject}, or <b>def</b> if no mapping the the given <b>key</b> exists or is not a
	 * {@code boolean}.
	 * 
	 * @param key The key
	 * @param def The default value
	 * @return The {@code boolean}
	 */
	public boolean optBoolean(String key, boolean def) {
		Object v = this.get(key);
		if(v instanceof Boolean)
			return (boolean) v;
		else
			return def;
	}


	@SuppressWarnings("unchecked")
	private <T> void addToList(List<T> list, Object o) {
		list.add((T) o);
	}

	/**
	 * Populates all fields with the {@link ConfigurationOption} annotation of the given {@code targetObject} with values from this {@code ConfigObject} using reflection.
	 * <p>
	 * The name of the field is the key used in this {@code ConfigObject} to get the value.
	 * If a value in this {@code ConfigObject} does not have the same (or similar) type of the corresponding field, a {@link ConfigurationException} is thrown.
	 * If no value for an option is present in this {@code ConfigObject}, a {@link ConfigurationException} is thrown for {@linkplain ConfigurationOption#required() required} options; field
	 * values of other options stay unchanged.
	 * <p>
	 * This method supports the following field type: all primitive types and their boxed types, {@code String}, {@link ConfigObject}, {@link ConfigArray}, {@link List}.
	 *
	 * @param targetObject The target object
	 * @throws ConfigurationException If a value has the incorrect type, or the value of a required option is missing
	 * @throws ConfigurationException If a reflective operation fails
	 * @throws UnsupportedOperationException If a field type is unsupported
	 * @since 2.10
	 */
	public void populateConfigurationOptions(Object targetObject){
		try{
			java.lang.reflect.Field[] fields = targetObject.getClass().getDeclaredFields();
			for(java.lang.reflect.Field f : fields){
				String name = f.getName();
				ConfigurationOption opt = f.getAnnotation(ConfigurationOption.class);
				if(opt == null)
					continue;
				if(this.containsKey(name)){
					f.setAccessible(true);
					if(f.getType() == List.class){
						java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) f.getGenericType();
						Class<?> genericType = (Class<?>) pt.getActualTypeArguments()[0];
						ConfigArray array = this.getArray(name);
						List<?> l = new java.util.ArrayList<>();
						for(int i = 0; i < array.size(); i++){
							Object o = convertObjectValue(array.get(i), genericType, name + "[" + i + "]");
							this.addToList(l, o);
						}
						f.set(targetObject, l);
					}else{
						Object data = this.get(name);
						Object value = convertObjectValue(data, f.getType(), name);
						f.set(targetObject, value);
					}
				}else if(opt.required())
					throw new ConfigurationException("Missing required option '" + name + "'");
			}
		}catch(ReflectiveOperationException e){
			throw new ConfigurationException("Reflective operation failed", e);
		}
	}


	/**
	 * Determines whether the given object is equal to this {@code ConfigObject}.
	 * <p>
	 * Another object is equal if it is also a {@code ConfigObject} and contains the same mappings.
	 * 
	 * @see Map#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ConfigObject))
			return false;
		return ((ConfigObject) o).data.equals(this.data);
	}

	/**
	 * Returns a hash code for this {@code ConfigObject}.
	 * 
	 * @see Map#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.data.hashCode();
	}

	/**
	 * Returns a clone of this {@code ConfigObject}, which contains the same elements as this {@code ConfigObject}.
	 */
	@Override
	public ConfigObject clone() {
		return new ConfigObject(this.data, true);
	}

	/**
	 * Returns a string representation of this {@code ConfigObject}.
	 */
	@Override
	public String toString() {
		if(this.isEmpty())
			return "{}";
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

	private static Object convertObjectValue(Object obj, Class<?> type, String name){
		try{
			if(type == short.class || type == Short.class){
				return ((Number) obj).shortValue();
			}else if(type == int.class || type == Integer.class){
				return ((Number) obj).intValue();
			}else if(type == long.class || type == Long.class){
				return ((Number) obj).longValue();
			}else if(type == float.class || type == Float.class){
				return ((Number) obj).floatValue();
			}else if(type == double.class || type == Double.class){
				return ((Number) obj).doubleValue();
			}else if(type == boolean.class || type == Boolean.class){
				return (boolean) obj;
			}else if(type == String.class){
				return String.valueOf(obj);
			}else if(type == ConfigObject.class){
				return (ConfigObject) obj;
			}else if(type == ConfigArray.class){
				return (ConfigArray) obj;
			}else
				throw new UnsupportedOperationException("Unsupported type '" + type + "'");
		}catch(ClassCastException | NullPointerException e){
			throw new ConfigurationException("Expected '" + type.getName() + "' for '" + name + "' but received value of type '" + getTypeName(obj) + "'");
		}
	}
}
