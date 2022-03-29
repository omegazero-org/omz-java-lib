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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Contains utility methods for use with reflection.
 * 
 * @since 2.1
 */
public final class ReflectionUtil {


	private ReflectionUtil() {
	}


	/**
	 * Returns the method type signature of the given {@link Method}.
	 * 
	 * @param m The method
	 * @return The type signature of the given method
	 */
	public static String getMethodSignature(Method m) {
		return getMethodSignature(m.getReturnType(), m.getParameterTypes());
	}

	/**
	 * Returns the method type signature of a method with the given <b>returnType</b> and <b>parameterTypes</b>.
	 * 
	 * @param returnType The return type
	 * @param parameterTypes The parameter types
	 * @return The type signature of a method with the given types
	 */
	public static String getMethodSignature(Class<?> returnType, Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder("(");
		if(parameterTypes != null)
			for(Class<?> cl : parameterTypes)
				sb.append(getSignatureOfClass(cl));
		sb.append(')').append(getSignatureOfClass(returnType));
		return sb.toString();
	}

	/**
	 * Returns the type signature string of the given type, for example "<code>I</code>" or "<code>Ljava/lang/String;</code>".
	 * 
	 * @param cl The class
	 * @return The type signature
	 */
	public static String getSignatureOfClass(Class<?> cl) {
		if(cl == void.class)
			return "V";
		String sig = Array.newInstance(cl, 0).toString();
		return sig.substring(1, sig.indexOf('@')).replace('.', '/');
	}


	/**
	 * Checks whether the given method has the given <b>name</b> and <b>parameterTypes</b>. A parameter type of the given method may also be a superclass of a given expected
	 * parameter type.
	 * 
	 * @param m The method
	 * @param name The method name to compare to
	 * @param parameterTypes The parameter types to compare to
	 * @return <code>true</code> if the given method has the given <b>name</b> and <b>parameterTypes</b>
	 */
	public static boolean isMethod(Method m, String name, Class<?>[] parameterTypes) {
		if(!m.getName().equals(name))
			return false;
		Class<?>[] mParameterTypes = m.getParameterTypes();
		if(mParameterTypes.length != parameterTypes.length)
			return false;
		for(int i = 0; i < mParameterTypes.length; i++){
			if(!mParameterTypes[i].isAssignableFrom(parameterTypes[i]))
				return false;
		}
		return true;
	}


	/**
	 * Returns an array containing the types of each object in the given <b>array</b>. If an element in the given array is <code>null</code>, the returned array will contain the
	 * <code>void</code> class at its position.
	 * 
	 * @param array The object array
	 * @return The array of types
	 * @since 2.7
	 */
	public static Class<?>[] getTypesFromObjectArray(Object[] array) {
		Class<?>[] types = new Class<?>[array.length];
		for(int i = 0; i < array.length; i++){
			if(array[i] == null)
				types[i] = void.class;
			else
				types[i] = array[i].getClass();
		}
		return types;
	}


	/**
	 * Gets the names of all {@code public static int} (possibly also {@code final}) fields starting with <b>prefix</b> of the given class.
	 * <p>
	 * The returned array contains all names without the <b>prefix</b>. The index of each string is the value of the integer field minus <b>lowest</b>. If an index is outside of
	 * the created string array with the given <b>length</b>, the name is not included in the array.
	 * <p>
	 * If <b>readableNames</b> is {@code true}, any {@code _} characters in the field names are converted to spaces, and all characters are set to lowercase, except the first
	 * character of each word.
	 * 
	 * @param cl The class
	 * @param prefix The prefix
	 * @param lowest The lowest integer value
	 * @param length The length of the returned array
	 * @param readableNames Whether to transform field names
	 * @return The string array
	 * @throws IllegalAccessException If a field is inaccessible
	 * @since 2.8
	 */
	public static String[] getIntegerFieldNames(Class<?> cl, String prefix, int lowest, int length, boolean readableNames) throws IllegalAccessException {
		String[] names = new String[length];
		Field[] fields = cl.getFields();
		final int prefixLen = prefix.length();
		for(Field field : fields){
			String name = field.getName();
			if(name.startsWith(prefix) && Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && field.getType() == int.class){
				int index = field.getInt(null) - lowest;
				if(index < 0 || index >= names.length)
					continue;
				String str;
				if(readableNames){
					char[] chars = new char[name.length() - prefixLen];
					boolean u = true;
					for(int i = prefixLen; i < name.length(); i++){
						char c = name.charAt(i);
						int ci = i - prefixLen;
						if(c == '_'){
							chars[ci] = ' ';
							u = true;
						}else{
							if(!u && Character.isUpperCase(c))
								c = Character.toLowerCase(c);
							chars[ci] = c;
							u = false;
						}
					}
					str = new String(chars);
				}else{
					str = name.substring(prefixLen);
				}
				names[index] = str;
			}
		}
		return names;
	}
}
