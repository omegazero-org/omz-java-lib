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

public final class ReflectionUtil {

	private ReflectionUtil() {
	}


	public static String getMethodSignature(Method m) {
		return getMethodSignature(m.getReturnType(), m.getParameterTypes());
	}

	public static String getMethodSignature(Class<?> returnType, Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder("(");
		if(parameterTypes != null)
			for(Class<?> cl : parameterTypes)
				sb.append(getSignatureOfClass(cl));
		sb.append(')').append(getSignatureOfClass(returnType));
		return sb.toString();
	}

	public static String getSignatureOfClass(Class<?> cl) {
		if(cl == void.class)
			return "V";
		String sig = Array.newInstance(cl, 0).toString();
		return sig.substring(1, sig.indexOf('@'));
	}


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
}
