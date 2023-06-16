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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
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
	 * @see #getSignatureOfClass(Class)
	 * @see #getClassesForSignature(String)
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
	 * Returns the array of {@code Class} objects for a given type signature list.
	 * <p>
	 * For example, "<code>ILjava/lang/String;</code>" returns an array containing the {@code Class} representing the primitive {@code int} type and the {@code Class} representing {@code String}.
	 * <p>
	 * The given string may also be a method type signature as returned by {@link #getMethodSignature(Class, Class[])}. The return type signature is ignored.
	 *
	 * @param sig The type signature list
	 * @return The array of class objects
	 * @throws ClassNotFoundException If a class in the given signature was not found
	 * @since 2.12.0
	 * @see #getClassForTypeSignature(String)
	 * @see #getMethodSignature(Class, Class[])
	 */
	public static Class<?>[] getClassesForSignature(String sig) throws ClassNotFoundException {
		List<Class<?>> classes = new java.util.ArrayList<>();
		int index = 0;
		if(sig.startsWith("("))
			index = 1;
		try{
			while(index < sig.length()){
				if(sig.charAt(index) == ')')
					break;
				int si = index;
				if(sig.charAt(index) == '['){
					while(sig.charAt(++index) == '[');
				}
				if(sig.charAt(index) == 'L'){
					index = sig.indexOf(';', si);
					if(index < 0)
						throw new IllegalArgumentException("Fully classified class name missing ';'");
					index++;
					classes.add(getClassForTypeSignature(sig.substring(si, index)));
				}else{
					index++;
					classes.add(getClassForTypeSignature(sig.substring(si, index)));
				}
			}
		}catch(IndexOutOfBoundsException e){
			throw new IllegalArgumentException(e.toString(), e);
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * Returns the type signature string of the given type, for example "<code>I</code>" or "<code>Ljava/lang/String;</code>".
	 * The inverse operation of {@link #getClassForTypeSignature(String)}.
	 * 
	 * @param cl The class
	 * @return The type signature
	 * @see #getMethodSignature(Class, Class[])
	 */
	public static String getSignatureOfClass(Class<?> cl) {
		if(cl == void.class)
			return "V";
		String sig = Array.newInstance(cl, 0).toString();
		return sig.substring(1, sig.indexOf('@')).replace('.', '/');
	}

	/**
	 * Returns the {@code Class} object for a given type signature. The inverse operation of {@link #getSignatureOfClass(Class)}.
	 * <p>
	 * For example, "<code>I</code>" returns the {@code Class} representing the primitive {@code int} type, or "<code>Ljava/lang/String;</code>" returns the {@code String} class object.
	 *
	 * @param sig The type signature
	 * @return The class object
	 * @throws ClassNotFoundException If no class could be found that is represented by the given signature
	 * @since 2.12.0
	 * @see #getClassesForSignature(String)
	 */
	public static Class<?> getClassForTypeSignature(String sig) throws ClassNotFoundException {
		sig = sig.replace('/', '.');
		if(sig.startsWith("[")){
			return Class.forName(sig);
		}else if(sig.startsWith("L") && sig.endsWith(";")){
			return Class.forName(sig.substring(1, sig.length() - 1));
		}else if(sig.length() == 1){
			switch(sig.charAt(0)){
				case 'Z': return boolean.class;
				case 'B': return byte.class;
				case 'C': return char.class;
				case 'S': return short.class;
				case 'I': return int.class;
				case 'J': return long.class;
				case 'F': return float.class;
				case 'D': return double.class;
				case 'V': return void.class;
				default: throw new ClassNotFoundException(sig);
			}
		}else
			throw new ClassNotFoundException(sig);
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


	/**
	 * Returns the {@code Class} object for the given class <b>name</b>.
	 * <p>
	 * Similar to {@link Class#forName(String)}, except that no exception is thrown when the class is not found, returning {@code null} instead.
	 *
	 * @param name The class name
	 * @return The {@code Class}, or {@code null} if the class was not found
	 * @since 2.12.0
	 */
	public static Class<?> getClass(String name){
		try{
			return Class.forName(name);
		}catch(ReflectiveOperationException e){
			return null;
		}
	}

	/**
	 * Reads the field identified by the given <b>identifier</b>, which has the format "{@code <full class name>#<field name>}".
	 * <p>
	 * The <b>instance</b> is the instance object of which the field is read, if the field is not static. This method allows accessing private fields.
	 * <p>
	 * For example, {@code getField("java.lang.String#hash", "")} returns {@code 0} (the initial value).
	 *
	 * @param identifier The field identifier
	 * @param instance The instance
	 * @return The value of the field (may be {@code null})
	 * @throws ReflectiveOperationException If the class or field does not exist, or field access failed
	 * @throws IllegalArgumentException If <b>instance</b> is not an instance of the class in the identifier
	 * @throws NullPointerException If <b>instance</b> is {@code null}, but the field is non-static
	 * @since 2.12.0
	 * @see #getFieldOptional(String, Object)
	 * @see #get(String, Object[])
	 * @see Field#get(Object)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getField(String identifier, Object instance) throws ReflectiveOperationException {
		int fnamesep = identifier.indexOf("#");
		if(fnamesep < 0)
			throw new IllegalArgumentException("Invalid identifier format");
		Class<?> cl = Class.forName(identifier.substring(0, fnamesep));
		Field field = cl.getDeclaredField(identifier.substring(fnamesep + 1));
		field.setAccessible(true);
		return (T) field.get(instance);
	}

	/**
	 * Reads the field identified by the given <b>identifier</b>. The same as {@link #getField(String, Object)}, except that an empty {@code Optional} is returned if an exception is thrown.
	 *
	 * @param identifier The field identifier
	 * @param instance The instance
	 * @return An {@code Optional} containing the value of the field (may be {@code null}), or an empty {@code Optional} if the field could not be accessed
	 * @since 2.12.0
	 * @see #getOptional(String, Object[])
	 */
	public static <T> Optional<T> getFieldOptional(String identifier, Object instance){
		try{
			return Optional.ofNullable(getField(identifier, instance));
		}catch(Exception e){
			return Optional.empty();
		}
	}

	/**
	 * Calls the method identified by the given <b>identifier</b>, which has the format "{@code <full class name>::<method signature>}", with the given <b>arguments</b>.
	 * <p>
	 * If the target method is non-static, the first argument of <b>arguments</b> is taken as the instance object used to call the method (and not explicitly passed to the method).
	 * This method allows accessing private methods.
	 * <p>
	 * The method part of the identifier (the part after the {@code ::}) consists of the method name, followed by a parameter type signature of the same format as
	 * {@link getClassesForSignature(String)} accepts, enclosed in parentheses (any return type after the closing parenthesis is ignored). If the method has no parameters, the parameter
	 * type signature parentheses may be omitted. If the method name equals the special string "{@code <init>}", the constructor of the class is called.
	 * <p>
	 * For example, {@code callMethod("java.lang.String::length", "example")} returns {@code 7} (the length of the string).
	 *
	 * @param identifier The method identifier
	 * @param arguments The arguments to pass to the method, including an object instance
	 * @return The return value of the method (may be {@code null}, for example for void methods)
	 * @throws ReflectiveOperationException If the class or method does not exist, method access failed, or the method threw an exception
	 * @throws IllegalArgumentException If <b>instance</b> is not an instance of the class in the identifier, or the given <b>arguments</b> do not match the method parameter list
	 * @throws NullPointerException If <b>instance</b> is {@code null}, but the method is non-static
	 * @since 2.12.0
	 * @see #callMethodOptional(String, Object[])
	 * @see #get(String, Object[])
	 * @see Method#invoke(Object, Object[])
	 */
	@SuppressWarnings("unchecked")
	public static <T> T callMethod(String identifier, Object... arguments) throws ReflectiveOperationException {
		int mnamesep = identifier.indexOf("::");
		if(mnamesep < 0)
			throw new IllegalArgumentException("Invalid identifier format");
		Class<?> cl = Class.forName(identifier.substring(0, mnamesep));
		String methodSig = identifier.substring(mnamesep + 2);
		int methodParamSep = methodSig.indexOf('(');
		String mname;
		Class<?>[] params;
		if(methodParamSep > 0){
			mname = methodSig.substring(0, methodParamSep);
			params = getClassesForSignature(methodSig.substring(methodParamSep));
		}else{
			mname = methodSig;
			params = new Class<?>[0];
		}
		if(mname.equals("<init>")){
			Constructor<?> constructor = cl.getDeclaredConstructor(params);
			constructor.setAccessible(true);
			return (T) constructor.newInstance(arguments);
		}else{
			Method method = cl.getDeclaredMethod(mname, params);
			method.setAccessible(true);
			Object instance = null;
			if(!Modifier.isStatic(method.getModifiers())){
				if(arguments.length == 0)
					throw new IllegalArgumentException("Method is not static but no arguments were passed");
				instance = arguments[0];
				arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
			}else if(arguments.length - 1 == params.length && cl.equals(arguments[0].getClass()))
				arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
			return (T) method.invoke(instance, arguments);
		}
	}

	/**
	 * Calls the method identified by the given <b>identifier</b>. The same as {@link #callMethod(String, Object[])}, except that an empty {@code Optional} is returned if an exception is thrown.
	 *
	 * @param identifier The method identifier
	 * @param arguments The arguments to pass to the method, including an object instance
	 * @return An {@code Optional} containing the return value of the method (may be {@code null}), or an empty {@code Optional} if the method could not be called
	 * @since 2.12.0
	 * @see #getOptional(String, Object[])
	 */
	public static <T> Optional<T> callMethodOptional(String identifier, Object... arguments){
		try{
			return Optional.ofNullable(callMethod(identifier, arguments));
		}catch(Exception e){
			return Optional.empty();
		}
	}

	/**
	 * Combines {@link #getClass(String)}, {@link #getField(String, Object)}, and {@link #callMethod(String, Object[])} into a single method.
	 * Which of the three methods is called is determined by the <b>identifier</b> format.
	 * <p>
	 * The meaning of <b>arguments</b> depends on the identifier. If the identifier is a method, <b>arguments</b> has the meaning as defined by {@link #callMethod(String, Object[])}.
	 * If it is a field, an optional first argument is used as the object instance passed to {@link #getField(String, Object)}. If the identifier is a class, <b>arguments</b> is ignored.
	 *
	 * @param identifier The identifier
	 * @param arguments The arguments
	 * @return The value of the identified object
	 * @throws ReflectiveOperationException If the class, field, or method does not exist, field or method access failed, or the method threw an exception
	 * @throws IllegalArgumentException If <b>instance</b> is not an instance of the class in the identifier, or the given <b>arguments</b> do not match the method parameter list
	 * @throws NullPointerException If <b>instance</b> is {@code null}, but the field or method is non-static
	 * @since 2.12.0
	 * @see #getOptional(String, Object[])
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String identifier, Object... arguments) throws ReflectiveOperationException {
		if(identifier.contains("::")){
			return callMethod(identifier, arguments);
		}else if(identifier.contains("#")){
			return getField(identifier, arguments.length > 0 ? arguments[0] : null);
		}else{
			return (T) Class.forName(identifier);
		}
	}

	/**
	 * The same as {@link #get(String, Object[])}, except that an empty {@code Optional} is returned if an exception is thrown.
	 *
	 * @param identifier The identifier
	 * @param arguments The arguments
	 * @return An {@code Optional} containing the value of the identified object, or an empty {@code Optional} if the value could not be retrieved
	 * @since 2.12.0
	 */
	public static <T> Optional<T> getOptional(String identifier, Object... arguments){
		try{
			return Optional.ofNullable(get(identifier, arguments));
		}catch(Exception e){
			return Optional.empty();
		}
	}
}
