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
import java.lang.reflect.Method;

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
	 * @param returnType     The return type
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
	 * Checks whether the given method has the given <b>name</b> and <b>parameterTypes</b>.
	 * 
	 * @param m              The method
	 * @param name           The method name to compare to
	 * @param parameterTypes The parameter types to compare to
	 * @return <code>true</code> if the given method has the given <b>name</b> and <b>parameterTypes</b>.
	 */
	public static boolean isMethod(Method m, String name, Class<?>[] parameterTypes) {
		if(!m.getName().equals(name))
			return false;
		Class<?>[] mParameterTypes = m.getParameterTypes();
		if(mParameterTypes.length != parameterTypes.length)
			return false;
		for(int i = 0; i < mParameterTypes.length; i++){
			if(!mParameterTypes[i].equals(parameterTypes[i]))
				return false;
		}
		return true;
	}


	/**
	 * Returns an array containing the types of each object in the given <b>array</b>. If an element in the given array is <code>null</code>, the returned array will contain
	 * the <code>void</code> class at its position.
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
}
