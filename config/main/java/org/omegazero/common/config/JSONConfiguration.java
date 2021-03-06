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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link Configuration} implementation based on JSON.
 * <p>
 * This class requires the {@code org.json} library, which can be obtained here: <a href="https://github.com/stleary/JSON-java">https://github.com/stleary/JSON-java</a>.
 */
public class JSONConfiguration implements Configuration {


	private final JSONObject json;

	/**
	 * Creates a new {@code JSONConfiguration}.
	 * 
	 * @param fileData A byte array representing a JSON object
	 * @throws JSONException If the given data is not valid JSON
	 */
	public JSONConfiguration(byte[] fileData) {
		this(new String(fileData));
	}

	/**
	 * Creates a new {@code JSONConfiguration}.
	 * 
	 * @param fileData A string representing a JSON object
	 * @throws JSONException If the given data is not valid JSON
	 */
	public JSONConfiguration(String fileData) {
		this.json = new JSONObject(fileData);
	}


	private Object convertValueOfObject(Object obj, Class<?> type, String name) {
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
				return convertJSONObject((JSONObject) obj);
			}else if(type == ConfigArray.class){
				return convertJSONArray((JSONArray) obj);
			}else
				return null;
		}catch(ClassCastException e){
			// can't use instanceof (or Class.isInstance(o)) for checking if type is valid, because number types may be freely converted using the Number class,
			// but Class.isInstance(o) will return false if it isn't the exact same type
			throw new IllegalArgumentException("'" + name + "' requires value of type '" + type.getName() + "' but received value of type '" + obj.getClass().getName() + "'");
		}
	}

	private <T> List<T> getArrayListOfType(Class<T> type) {
		return new ArrayList<T>();
	}

	@SuppressWarnings("unchecked")
	private <T> void addToList(List<T> list, Object o) {
		list.add((T) o);
	}


	/**
	 * Method that is called when a field with an unsupported type is encountered.
	 * 
	 * @param field The field to be set
	 * @param jsonObject The object returned by the JSON library for the key of the field
	 * @return <code>true</code> if population was successful, <code>false</code> otherwise
	 * @see JSONConfiguration#load()
	 */
	protected boolean setUnsupportedField(Field field, Object jsonObject) {
		return false;
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * The JSON file that was parsed in the constructor is read and fields that have the {@link ConfigurationOption} annotation will be populated if the JSON file contains a key
	 * with the same name as the field. Additional keys in the JSON file that have no corresponding field are ignored.<br>
	 * If the value of a key is a different type than the field type, an <code>IllegalArgumentException</code> is thrown.
	 * <p>
	 * Supported field types are <code>ConfigObject</code>, <code>ConfigArray</code>, <code>String</code>, any primitive type and {@link List}s of the mentioned types. If the field
	 * type is not supported, the overridable method {@link JSONConfiguration#setUnsupportedField(Field, Object)} is called. If this method is not implemented or returns
	 * <code>false</code> (the default behavior), an <code>UnsupportedOperationException</code> is thrown.
	 */
	@Override
	public void load() throws IOException {
		try{
			Field[] fields = this.getClass().getDeclaredFields();
			for(Field f : fields){
				String name = f.getName();
				if(f.isAnnotationPresent(ConfigurationOption.class) && this.json.has(name)){
					f.setAccessible(true);
					if(f.getType() == List.class){
						ParameterizedType pt = (ParameterizedType) f.getGenericType();
						Class<?> genericType = (Class<?>) pt.getActualTypeArguments()[0];
						List<?> l = this.getArrayListOfType(genericType);
						f.set(this, l);
						JSONArray jsonArray = this.json.getJSONArray(name);
						for(int i = 0; i < jsonArray.length(); i++){
							Object o = JSONConfiguration.this.convertValueOfObject(jsonArray.get(i), genericType, name + "[" + i + "]");
							if(o == null){ // the generic type of this list is not supported
								this.setUnsupportedField(f, jsonArray);
								break;
							}else if(o == JSONObject.NULL)
								o = null;
							JSONConfiguration.this.addToList(l, o);
						}
					}else{
						Object data = this.json.get(name);
						Object value = this.convertValueOfObject(data, f.getType(), name);
						if(data == JSONObject.NULL){
							f.set(this, null);
						}else if(value != null){
							f.set(this, value);
						}else if(!this.setUnsupportedField(f, data)){
							throw new UnsupportedOperationException("Field '" + name + "' is of unsupported type '" + f.getType() + "'");
						}
					}
				}
			}
		}catch(ReflectiveOperationException e){
			throw new IOException("Reflective operation failed", e);
		}
	}


	/**
	 * Converts the given {@link JSONObject} to a {@link ConfigObject}.
	 * 
	 * @param json The <code>JSONObject</code> to convert
	 * @return The resulting <code>ConfigObject</code>
	 * @since 2.4
	 */
	public static ConfigObject convertJSONObject(JSONObject json) {
		Map<String, Object> data = new HashMap<>();
		for(String k : json.keySet()){
			data.put(k, convertJSONValue(json.get(k)));
		}
		return new ConfigObject(data);
	}

	/**
	 * Converts the given {@link JSONArray} to a {@link ConfigArray}.
	 * 
	 * @param json The <code>JSONArray</code> to convert
	 * @return The resulting <code>ConfigArray</code>
	 * @since 2.4
	 */
	public static ConfigArray convertJSONArray(JSONArray json) {
		List<Object> cdata = new ArrayList<>(json.length());
		Iterator<Object> data = json.iterator();
		while(data.hasNext())
			cdata.add(convertJSONValue(data.next()));
		return new ConfigArray(cdata);
	}

	/**
	 * Converts the given, possibly JSON-specific, object <b>v</b> to an abstracted data representation.
	 * 
	 * @param v The object to convert
	 * @return The resulting object
	 * @since 2.4
	 * @see #convertJSONObject(JSONObject)
	 * @see #convertJSONArray(JSONArray)
	 */
	public static Object convertJSONValue(Object v) {
		if(v instanceof JSONObject){
			return convertJSONObject((JSONObject) v);
		}else if(v instanceof JSONArray){
			return convertJSONArray((JSONArray) v);
		}else if(v == JSONObject.NULL)
			return null;
		else
			return v;
	}
}
