/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.runtime;

import java.util.Objects;
import java.util.function.Supplier;

import org.omegazero.common.OmzLib;
import org.omegazero.common.event.Tasks;
import org.omegazero.common.logging.Logger;
import org.omegazero.common.logging.LoggerUtil;
import org.omegazero.common.util.Args;
import org.omegazero.common.util.PropertyUtil;
import org.omegazero.common.util.Util;

/**
 * Provides standard runtime setup procedures for the standalone application running in this runtime.
 * <p>
 * To use this class, the application must implement the {@link Application} interface, which declares a startup and shutdown method, each called once at the start and end of the
 * application lifecycle.
 * <p>
 * All methods in this class are static because the methods are each intended to be called only once during the lifetime of the application. This class does significant changes to
 * the runtime and must not be used in a managed container (for example on an application server).
 * 
 * @since 2.9
 */
public class ApplicationWrapper {

	private static final Logger logger = Logger.create();


	private static Application app;
	private static Supplier<Args> argumentParser;

	private static boolean started;
	private static boolean shuttingDown;

	private static int shutdownTimeout;


	/**
	 * Initializes the {@code ApplicationWrapper} by setting the {@link Application} instance from the given supplier.
	 * <p>
	 * Equivalent to a call to:
	 * 
	 * <pre>
	 * {@link #init(Application) init}(instanceSupplier.get())
	 * </pre>
	 * 
	 * @param instanceSupplier The application instance supplier
	 */
	public static void init(Supplier<Application> instanceSupplier) {
		init(instanceSupplier.get());
	}

	/**
	 * Initializes the {@code ApplicationWrapper} by setting the {@link Application} instance.
	 * <p>
	 * Equivalent to a call to:
	 * 
	 * <pre>
	 * {@link #init(Application, Supplier) init}(app, null)
	 * </pre>
	 * 
	 * @param app The application instance
	 */
	public static void init(Application app) {
		init(app, null);
	}

	/**
	 * Initializes the {@code ApplicationWrapper} by setting the {@link Application} instance and an optional custom {@link Args} parser.
	 * 
	 * @param app The application instance
	 * @param argumentParser The {@code Args} parser to use to parse the process startup arguments. May be {@code null} to use {@link Args.DefaultFormat}
	 * @throws IllegalStateException If {@link #start(String[])} was called successfully already
	 */
	public static synchronized void init(Application app, Supplier<Args> argumentParser) {
		if(started)
			throw new IllegalStateException("Already started");
		ApplicationWrapper.app = Objects.requireNonNull(app);
		if(argumentParser == null)
			ApplicationWrapper.argumentParser = Args.DefaultFormat::new;
		else
			ApplicationWrapper.argumentParser = argumentParser;
	}

	/**
	 * Starts the application.
	 * <p>
	 * This method calls {@link Application#start(Args)} in the configured application with the parsed startup arguments.
	 * <p>
	 * Requires a call to {@link #init(Application, Supplier)} to set the application instance and an optional argument parser.
	 * <p>
	 * This method can only be called once successfully.
	 * 
	 * @param pargs The process startup arguments, passed to the {@code main} method
	 * @throws IllegalStateException If this method was called successfully already
	 */
	public static synchronized void start(String[] pargs) {
		if(started)
			throw new IllegalStateException("Already started");
		if(app == null)
			throw new IllegalStateException("init was never called");
		started = true;
		Args args = argumentParser.get();
		args.parseArguments(pargs);

		LoggerUtil.redirectStandardOutputStreams();

		String logFile = args.getValueOrDefault("logFile", "log");
		LoggerUtil.init(LoggerUtil.resolveLogLevel(args.getValue("logLevel")), logFile.equals("null") ? null : logFile);

		Util.onClose(ApplicationWrapper::shutdown);

		OmzLib.printBrand();
		Thread.setDefaultUncaughtExceptionHandler(new LocalUncaughtExceptionHandler());

		try{
			logger.info("Starting ", app);
			app.start(args);
		}catch(Throwable e){
			logger.fatal("Error during initialization: ", e);
		}
	}

	/**
	 * Shuts down the application and runtime.
	 * <p>
	 * This method calls {@link Application#close()} and other exit methods, and will then wait at most for the number of milliseconds set in the
	 * {@code org.omegazero.common.runtime.shutdownTimeout} system property for the runtime to exit. If this timeout is exceeded, the runtime is forcibly terminated and a warning
	 * message with debug information is printed. The default value is {@code 2000} milliseconds.
	 * <p>
	 * This method can be called any number of times, but only the first successful invocation has any effect.
	 * 
	 * @throws IllegalStateException If {@link #start(String[])} was never called successfully
	 */
	public static synchronized void shutdown() {
		if(!started)
			throw new IllegalStateException("Not started");
		try{
			// this function may be called multiple times unintentionally, for example when this method is called explicitly (through shutdown()),
			// it shuts down all non-daemon threads, causing shutdown hooks to execute, one of which (at least, in Util.onClose) will also call this method
			if(shuttingDown)
				return;
			shuttingDown = true;

			shutdownTimeout = PropertyUtil.getInt("org.omegazero.common.runtime.shutdownTimeout", 2000);

			logger.info("Shutting down");

			Tasks.I.timeout(ApplicationWrapper::shutdownTimeout, shutdownTimeout).daemon();

			app.close();

			LoggerUtil.close();
			Tasks.exit();
		}catch(Throwable e){
			logger.fatal("Error during shutdown: ", e);
		}finally{
			if(!Util.waitForNonDaemonThreads(shutdownTimeout))
				shutdownTimeout();
		}
	}


	/**
	 * Returns whether the runtime is shutting down ({@link #shutdown()} was called).
	 * 
	 * @return {@code true} if shutting down
	 */
	public static boolean isShuttingDown() {
		return shuttingDown;
	}


	private static void shutdownTimeout() {
		try{
			logger.warn("A non-daemon thread has not exited " + shutdownTimeout + " milliseconds after an exit request was issued, JVM will be forcibly terminated");
			for(Thread t : Thread.getAllStackTraces().keySet()){
				if(!t.isDaemon() && !"DestroyJavaVM".equals(t.getName()) && Thread.currentThread() != t){
					logger.warn("Still running thread (stack trace below): " + t.getName());
					for(StackTraceElement ste : t.getStackTrace())
						logger.warn("    " + ste);
				}
			}
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			Runtime.getRuntime().halt(2);
		}
	}


	private static class LocalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		private static final boolean exitOnDoubleFault = PropertyUtil.getBoolean("org.omegazero.common.runtime.exitOnDoubleFault", true);
		private static final byte[] vmerrMsg = "Virtual Machine Error\n".getBytes();
		private static final byte[] oomMsg = "Out Of Memory\n".getBytes();
		private static final byte[] dfMsg = "Uncaught error in exception handler\n".getBytes();


		@Override
		public void uncaughtException(Thread t, Throwable err) {
			try{
				logger.fatal("Uncaught exception in thread '", t.getName(), "': ", err);
				ApplicationWrapper.shutdown();
			}catch(VirtualMachineError e){ // things have really gotten out of hand now
				handleError(e, vmerrMsg);
				throw e;
			}catch(Throwable e){
				handleError(e, dfMsg);
				throw e;
			}
		}


		private static void handleError(Throwable err, byte[] msg) {
			try{
				System.setErr(LoggerUtil.sysErr);
				err.printStackTrace();
			}catch(OutOfMemoryError e){
				for(int i = 0; i < oomMsg.length; i++)
					LoggerUtil.sysOut.write(oomMsg[i]);
			}catch(Throwable e){
				for(int i = 0; i < vmerrMsg.length; i++)
					LoggerUtil.sysOut.write(vmerrMsg[i]);
			}finally{
				// exceptions thrown in the uncaught exception handler don't cause the VM to exit, even if it is a OOM error, so just exit manually
				// (because everything is definitely in a very broken state and it is easier for supervisor programs to detect that there is a problem when exiting)
				// exitOnDoubleFault option can be set to false for debugging
				if(exitOnDoubleFault)
					Runtime.getRuntime().halt(3);
			}
		}
	}
}
