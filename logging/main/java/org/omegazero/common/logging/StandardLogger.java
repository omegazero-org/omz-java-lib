/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.logging;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.omegazero.common.util.PropertyUtil;
import org.omegazero.common.util.Util;

/**
 * The standard {@link Logger} implementation returned by {@link Logger#create()}.
 *
 * @since 2.10
 */
public final class StandardLogger implements Logger {

	/**
	 * System property <code>org.omegazero.common.logging.stackTraces.classSource</code>
	 * <p>
	 * Enables logging of the source directory or JAR file of the class in each stack frame in stack traces.
	 * <p>
	 * <b>Default:</b> {@code true}
	 *
	 * @since 2.11.0
	 */
	public static final boolean ENABLE_ST_CLASS_SOURCE = PropertyUtil.getBoolean("org.omegazero.common.logging.stackTraces.classSource", true);
	/**
	 * System property <code>org.omegazero.common.logging.stackTraces.classSourceFull</code>
	 * <p>
	 * If {@link #ENABLE_ST_CLASS_SOURCE} is {@code true}, enables logging of the full source path, instead of just the file name.
	 * <p>
	 * <b>Default:</b> {@code false}
	 *
	 * @since 2.11.0
	 */
	public static final boolean ENABLE_ST_CLASS_SOURCE_FULL = PropertyUtil.getBoolean("org.omegazero.common.logging.stackTraces.classSourceFull", false);
	/**
	 * System property <code>org.omegazero.common.logging.stackTraces.xstVerbose</code>
	 * <p>
	 * Enables verbose logging of stack frames using the {@code co.paralleluniverse:extended-stacktrace} library, if available.
	 * <p>
	 * <b>Default:</b> {@code false}
	 *
	 * @since 2.11.0
	 */
	public static final boolean ENABLE_XST_VERBOSE = PropertyUtil.getBoolean("org.omegazero.common.logging.stackTraces.xstVerbose", false);

	private static Function<Throwable, StackTraceElement[]> throwableGetStackTraceInternal = StandardLogger::throwableGetStackTraceInternal;
	private static Method throwableGetStackTraceInternalMethod;
	private static boolean xstAvailable = true;
	private static Class<?> xst_ExtendedStackTrace;
	private static Method xst_ExtendedStackTrace_of;
	private static Class<?> xst_ExtendedStackTraceElement;
	private static Method[] xst_ExtendedStackTraceElement__methods;

	private final String fullClassName;
	private final String label;

	StandardLogger(String fullClassName) {
		this(fullClassName, fullClassName);
	}

	StandardLogger(Class<?> creator) {
		this(creator.getName(), creator.getSimpleName());
	}

	StandardLogger(String fullClassName, String label) {
		this.fullClassName = fullClassName;
		this.label = label;
	}


	/**
	 * Prints a stack trace of the given {@code throwable} to the given {@code Consumer}. Used by this {@code StandardLogger} to print stack traces.
	 * <p>
	 * The consumer is called for each line output.
	 * <p>
	 * This method has an output format similar to {@link Throwable#printStackTrace()}. It also attempts to find and output additional information about each stack frame, for example the source
	 * directory or JAR file of the class file. This behavior may be configured using system properties defined in this class, for example {@link #ENABLE_ST_CLASS_SOURCE}.
	 *
	 * @param throwable The {@code Throwable}
	 * @param println The output consumer
	 */
	public static void printStackTrace(Throwable throwable, Consumer<String> println){
		printStackTrace0(throwable, println, new java.util.HashSet<>(), null, "", "");
	}

	private static void printStackTrace0(Throwable throwable, Consumer<String> println, Set<Throwable> seen, WrappedStackTraceElement[] outer, String pre, String cap){
		if(seen.contains(throwable)){
			println.accept("<<circular reference>> " + pre + cap + throwable.toString());
			return;
		}
		seen.add(throwable);
		println.accept(pre + cap + throwable.toString());
		WrappedStackTraceElement[] trace = throwableGetStackTrace(throwable);
		int endIndex = trace.length - 1;
		if(outer != null){
			int endIndexOuter = outer.length - 1;
			while(endIndex >= 0 && endIndexOuter >= 0 && trace[endIndex].equals(outer[endIndexOuter])){
				endIndex--;
				endIndexOuter--;
			}
		}
		int commonElements = trace.length - 1 - endIndex;
		for(int i = 0; i <= endIndex; i++){
			WrappedStackTraceElement element = trace[i];
			String line = pre + "\tat " + element;
			if(ENABLE_ST_CLASS_SOURCE){
				String sourceFile = null;
				String version = null;
				Class<?> classObj = element.getDeclaringClass();
				if(classObj != null){
					java.security.CodeSource codeSource = classObj.getProtectionDomain().getCodeSource();
					if(codeSource != null && codeSource.getLocation() != null){
						sourceFile = codeSource.getLocation().toString();
						if(sourceFile.startsWith("file:/") && !ENABLE_ST_CLASS_SOURCE_FULL){
							int si = sourceFile.lastIndexOf('/');
							if(si == sourceFile.length() - 1)
								si = sourceFile.lastIndexOf('/', sourceFile.length() - 2);
							if(si >= 0)
								sourceFile = sourceFile.substring(si + 1);
						}
					}
					if(classObj.getPackage() != null)
						version = classObj.getPackage().getImplementationVersion();
				}
				if(sourceFile == null)
					sourceFile = "?";
				if(version == null)
					version = "?";
				line += (element.isDeclaringClassCertain() ? " [" : " ~[") + sourceFile + ":" + version + "]";
			}
			println.accept(line);
		}
		if(commonElements > 0)
			println.accept(pre + "\t... " + commonElements + " more");

		for(Throwable suppressedThrowable : throwable.getSuppressed()){
			printStackTrace0(suppressedThrowable, println, seen, trace, pre + "\t", "Suppressed: ");
		}
		Throwable cause = throwable.getCause();
		if(cause != null)
			printStackTrace0(cause, println, seen, trace, pre, "Caused by: ");
	}

	private static StackTraceElement[] throwableGetStackTraceInternal(Throwable throwable){
		try{
			if(throwableGetStackTraceInternalMethod == null){
				throwableGetStackTraceInternalMethod = Throwable.class.getDeclaredMethod("getOurStackTrace");
				throwableGetStackTraceInternalMethod.setAccessible(true);
			}
			return (StackTraceElement[]) throwableGetStackTraceInternalMethod.invoke(throwable);
		}catch(Exception e){
			if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
				LoggerUtil.sysErr.println("StandardLogger: Using Throwable.getOurStackTrace failed: " + e);
			throwableGetStackTraceInternal = Throwable::getStackTrace;
			return throwable.getStackTrace();
		}
	}

	private static WrappedStackTraceElement[] throwableGetStackTrace(Throwable throwable){
		StackTraceElement[] builtinStackTrace = throwableGetStackTraceInternal.apply(throwable);
		WrappedStackTraceElement[] stackTrace = new WrappedStackTraceElement[builtinStackTrace.length];
		boolean populated = false;
		if(xstAvailable){
			try{
				if(xst_ExtendedStackTrace == null)
					xst_ExtendedStackTrace = Class.forName("co.paralleluniverse.xst.ExtendedStackTrace");
				if(xst_ExtendedStackTrace_of == null)
					xst_ExtendedStackTrace_of = xst_ExtendedStackTrace.getMethod("of", Throwable.class);
				if(xst_ExtendedStackTraceElement == null)
					xst_ExtendedStackTraceElement = Class.forName("co.paralleluniverse.xst.ExtendedStackTraceElement");
				if(xst_ExtendedStackTraceElement__methods == null){
					Method[] methods = new Method[7];
					methods[0] = xst_ExtendedStackTraceElement.getMethod("getFileName");
					methods[1] = xst_ExtendedStackTraceElement.getMethod("getLineNumber");
					methods[2] = xst_ExtendedStackTraceElement.getMethod("getClassName");
					methods[3] = xst_ExtendedStackTraceElement.getMethod("getMethodName");
					methods[4] = xst_ExtendedStackTraceElement.getMethod("getDeclaringClass");
					methods[5] = xst_ExtendedStackTraceElement.getMethod("getMethod");
					methods[6] = xst_ExtendedStackTraceElement.getMethod("toString");
					xst_ExtendedStackTraceElement__methods = methods;
				}
				@SuppressWarnings("unchecked")
				Iterable<Object> extStackTrace = (Iterable<Object>) xst_ExtendedStackTrace_of.invoke(null, throwable);
				int i = 0;
				for(Object xste : extStackTrace)
					stackTrace[i++] = new WrappedExtendedStackTraceElement(xste);
				populated = true;
			}catch(ReflectiveOperationException | NoClassDefFoundError e){
				if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
					LoggerUtil.sysErr.println("StandardLogger: Assuming xst is not available: " + e);
				xstAvailable = false;
			}
		}
		if(!populated){
			for(int i = 0; i < stackTrace.length; i++)
				stackTrace[i] = new WrappedBuiltinStackTraceElement(builtinStackTrace[i]);
		}
		return stackTrace;
	}


	private static abstract class WrappedStackTraceElement {

		public abstract Class<?> getDeclaringClass();

		public abstract boolean isDeclaringClassCertain();

		public abstract Object getWrappedObject();

		@Override
		public String toString(){
			return this.getWrappedObject().toString();
		}

		@Override
		public boolean equals(Object o){
			if(!(o instanceof WrappedStackTraceElement))
				return false;
			return this.getWrappedObject().equals(((WrappedStackTraceElement) o).getWrappedObject());
		}
	}

	private static class WrappedBuiltinStackTraceElement extends WrappedStackTraceElement {

		private final StackTraceElement element;

		private Class<?> classObj;

		public WrappedBuiltinStackTraceElement(StackTraceElement element){
			this.element = element;
		}

		@Override
		public Class<?> getDeclaringClass(){
			if(this.classObj == null){
				try{
					this.classObj = Class.forName(this.element.getClassName());
				}catch(ClassNotFoundException e){
				}
			}
			return this.classObj;
		}

		@Override
		public boolean isDeclaringClassCertain(){
			return false;
		}

		@Override
		public Object getWrappedObject(){
			return this.element;
		}
	}

	private static class WrappedExtendedStackTraceElement extends WrappedStackTraceElement {

		private final Object xstElement;

		public WrappedExtendedStackTraceElement(Object xstElement) throws ReflectiveOperationException {
			this.xstElement = xstElement;
		}

		@Override
		public Class<?> getDeclaringClass(){
			try{
				return (Class<?>) xst_ExtendedStackTraceElement__methods[4].invoke(this.xstElement);
			}catch(ReflectiveOperationException e){
				if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
					LoggerUtil.sysErr.println("StandardLogger: WrappedExtendedStackTraceElement.getDeclaringClass failed: " + e);
				return null;
			}
		}

		@Override
		public boolean isDeclaringClassCertain(){
			return this.xstElement.getClass().getName().equals("co.paralleluniverse.xst.ExtendedStackTraceHotSpot$HotSpotExtendedStackTraceElement");
		}

		@Override
		public Object getWrappedObject(){
			return this.xstElement;
		}

		@Override
		public String toString() {
			try{
				if(ENABLE_XST_VERBOSE){
					xst_ExtendedStackTraceElement__methods[5].invoke(this.xstElement); // invoke getMethod to populate method field in ExtendedStackTraceElement (-> toString prints more info)
					return (String) xst_ExtendedStackTraceElement__methods[6].invoke(this.xstElement);
				}else{
					StringBuilder sb = new StringBuilder();
					sb.append(xst_ExtendedStackTraceElement__methods[2].invoke(this.xstElement)).append('.').append(xst_ExtendedStackTraceElement__methods[3].invoke(this.xstElement));
					String fileName = (String) xst_ExtendedStackTraceElement__methods[0].invoke(this.xstElement);
					int lineNumber = (int) xst_ExtendedStackTraceElement__methods[1].invoke(this.xstElement);
					sb.append('(');
					if(lineNumber == -2){
						sb.append("Native Method");
					}else if(fileName != null){
						sb.append(fileName);
						if(lineNumber >= 0)
							sb.append(':').append(lineNumber);
					}else
						sb.append("Unknown Source");
					sb.append(')');
					return sb.toString();
				}
			}catch(ReflectiveOperationException e){
				if(LoggerUtil.ENABLE_INTERNAL_DEBUG)
					LoggerUtil.sysErr.println("StandardLogger: WrappedExtendedStackTraceElement.toString failed: " + e);
				return this.xstElement.toString();
			}
		}
	}


	@Override
	public void log(LogLevel level, Object... obj) {
		boolean logEnabled = this.isLogging(level);
		if(!LoggerUtil.needAllLogMessages() && !logEnabled)
			return;
		StringBuilder sb = new StringBuilder(32);
		sb.append(Util.getFormattedTime()).append(' ');
		sb.append('[').append(level).append(']').append(' ');
		sb.append('[').append(Thread.currentThread().getName()).append(']').append(' ');
		sb.append('[').append(this.label).append(']').append(' ');
		for(Object o : obj){
			if(o instanceof Throwable){
				String lineSep = PropertyUtil.getString("line.separator", "\n");
				printStackTrace((Throwable) o, (line) -> {
					sb.append(line).append(lineSep);
				});
			}else
				sb.append(o);
		}
		String s = sb.toString();
		LoggerUtil.logToListeners(level, s);
		if(!logEnabled)
			return;
		LoggerUtil.newLogMessage(s, level.color());
	}


	/**
	 * {@inheritDoc}
	 * <p>
	 * This is the short name of the class this <code>StandardLogger</code> is bound to.
	 */
	@Override
	public String getLabel(){
		return this.label;
	}


	@Override
	public boolean isLogging(LogLevel level) {
		return !LoggerUtil.isLoggerMuted(this.fullClassName) && LoggerUtil.getLogLevel().level() >= level.level();
	}


	/**
	 * Returns the full name of the class this <code>StandardLogger</code> is bound to.
	 *
	 * @return The full class name
	 * @since 2.5
	 */
	public String getFullClassName() {
		return this.fullClassName;
	}
}
