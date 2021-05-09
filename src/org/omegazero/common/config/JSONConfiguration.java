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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * {@link Configuration} implementation based on JSON.<br>
 * <br>
 * This class requires the org.json library, which can be obtained here: <a href="https://github.com/stleary/JSON-java">https://github.com/stleary/JSON-java</a>.
 */
public class JSONConfiguration implements Configuration {

	private final JSONObject json;

	/**
	 * 
	 * @param fileData A byte array representing a JSON object. This data is parsed directly in the constructor
	 */
	public JSONConfiguration(byte[] fileData) {
		this(new String(fileData));
	}

	/**
	 * 
	 * @param fileData A string representing a JSON object. This data is parsed directly in the constructor
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
			}else if(type == String.class){
				return obj;
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
	 * @param field      The field to be set
	 * @param jsonObject The object returned by the JSON library for the key of the field
	 * @return <code>true</code> if population was successful, <code>false</code> otherwise
	 * @see JSONConfiguration#load()
	 */
	protected boolean setUnsupportedField(Field field, Object jsonObject) {
		return false;
	}


	/**
	 * {@inheritDoc}<br>
	 * <br>
	 * The JSON file that was parsed in the constructor is read and fields that have the {@link ConfigurationOption} annotation will be populated if the JSON file contains a key
	 * with the same name as the field. Additional keys in the JSON file that have no corresponding field are ignored.<br>
	 * If the value of a key is a different type than the field type, an <code>IllegalArgumentException</code> is thrown.<br>
	 * <br>
	 * Supported field types are <code>String</code>, any primitive type and {@link List}s of <code>String</code>s or primitive types. If the field type is not supported, the
	 * overridable method {@link JSONConfiguration#setUnsupportedField(Field, Object)} is called. If this method is not implemented or returns <code>false</code> (the default
	 * behavior), an <code>UnsupportedOperationException</code> is thrown.
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
						JSONArray jsonArray = this.json.getJSONArray(name);
						for(int i = 0; i < jsonArray.length(); i++){
							JSONConfiguration.this.addToList(l, JSONConfiguration.this.convertValueOfObject(jsonArray.get(i), genericType, name + "[" + i + "]"));
						}
						f.set(this, l);
					}else{
						Object data = this.json.get(name);
						Object value = this.convertValueOfObject(data, f.getType(), name);
						if(value != null){
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
}
