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

import java.util.Calendar;
import java.util.Random;

public final class Util {

	private static final String dayLabels[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
	private static final String monthLabels[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	private static final Random random = new Random();

	private Util() {
	}


	/**
	 * Formats the current time in this format:<br>
	 * <code>(weekday) (month) DD YYYY HH:MM:SS.mmm</code><br>
	 * where <code>(weekday)</code> is the three-letter shortened name of the current weekday (<code>Mon</code>, <code>Tue</code>, etc) and <code>(month)</code> the three-letter
	 * shortened name of the current month (<code>Jan</code>, <code>Feb</code>, etc).
	 * 
	 * @return Formatted time
	 */
	public static String getFormattedTime() {
		Calendar c = Calendar.getInstance();
		StringBuilder sb = new StringBuilder(32);
		int dow = c.get(Calendar.DAY_OF_WEEK);
		sb.append(dayLabels[dow - 1]).append(' ');
		sb.append(monthLabels[c.get(Calendar.MONTH)]).append(' ');
		sb.append(c.get(Calendar.DAY_OF_MONTH)).append(' ');
		sb.append(c.get(Calendar.YEAR)).append(' ');

		int hour = c.get(Calendar.HOUR_OF_DAY);
		if(hour < 10)
			sb.append('0');
		sb.append(hour).append(':');

		int minute = c.get(Calendar.MINUTE);
		if(minute < 10)
			sb.append('0');
		sb.append(minute).append(':');

		int second = c.get(Calendar.SECOND);
		if(second < 10)
			sb.append('0');
		sb.append(second).append('.');

		long millis = System.currentTimeMillis() % 1000;
		if(millis < 10)
			sb.append('0');
		if(millis < 100)
			sb.append('0');
		sb.append(millis);
		return sb.toString();
	}


	/**
	 * Adds a shutdown hook which runs the given <tt>Runnable</tt>.
	 * 
	 * @param handler The handler to run when the runtime is about to exit
	 * @see Util#onClose(Runnable, boolean)
	 */
	public static void onClose(Runnable handler) {
		Util.onClose(handler, false);
	}

	/**
	 * Adds a shutdown hook which runs the given <tt>Runnable</tt>.
	 * 
	 * @param handler          The handler to run when the runtime is about to exit
	 * @param waitForNonDaemon If <b>true</b> and the shutdown was triggered, <b>handler</b> will only be run if all other non-daemon threads have exited
	 */
	public static void onClose(Runnable handler, boolean waitForNonDaemon) {
		Thread t = new Thread(){

			@Override
			public void run() {
				while(waitForNonDaemon && Util.nonDaemonThreadRunning())
					try{
						Thread.sleep(100);
					}catch(InterruptedException e){
						break;
					}
				handler.run();
			}
		};
		t.setName("ShutdownThread");
		t.setPriority(Thread.MIN_PRIORITY);
		Runtime.getRuntime().addShutdownHook(t);
	}

	/**
	 * Checks if there is at least one non-daemon thread running.<br>
	 * The caller thread and the "DestroyJavaVM" thread are excluded from this check.
	 * 
	 * @return <b>true</b> if there is at least one non-daemon thread running apart from the caller and "DestroyJavaVM" thread
	 */
	public static boolean nonDaemonThreadRunning() {
		long thisId = Thread.currentThread().getId();
		boolean r = false;
		for(Thread t : Thread.getAllStackTraces().keySet()){
			if(t.getId() != thisId && !t.isDaemon() && !"DestroyJavaVM".equals(t.getName())){
				r = true;
				break;
			}
		}
		return r;
	}

	/**
	 * Blocks this thread until all non-daemon threads apart from the caller and "DestroyJavaVM" thread have exited ({@link Util#nonDaemonThreadRunning()} returns <b>false</b>).
	 * 
	 * @see Util#waitForNonDaemonThreads(int)
	 */
	public static void waitForNonDaemonThreads() {
		Util.waitForNonDaemonThreads(0);
	}

	/**
	 * Blocks this thread until all non-daemon threads apart from the caller and "DestroyJavaVM" thread have exited ({@link Util#nonDaemonThreadRunning()} returns <b>false</b>), or
	 * at least <b>timeout</b> milliseconds have passed.<br>
	 * <br>
	 * If the caller thread is interrupted while waiting, this method returns.
	 * 
	 * @implNote The poll interval is 50 milliseconds, meaning this method may not return immediately after all said threads have exited.
	 * @param timeout Maximum amount of time to wait for non-daemon threads to exit, in milliseconds. May be 0 to wait for an unlimited amount of time
	 * @return <b>true</b> if this function returns because all non-daemon threads have exited, or <b>false</b> if the timeout was exceeded or this thread was interrupted
	 */
	public static boolean waitForNonDaemonThreads(int timeout) {
		long start = System.currentTimeMillis();
		while(Util.nonDaemonThreadRunning()){
			if(timeout > 0 && System.currentTimeMillis() - start > timeout)
				return false;
			try{
				Thread.sleep(50);
			}catch(InterruptedException e){
				return false;
			}
		}
		return true;
	}


	public static String randomHex(int length) {
		StringBuilder sb = new StringBuilder(length);
		int l = 0;
		while(l < length){
			String s = Long.toHexString(random.nextLong());
			sb.append(s);
			l += s.length();
		}
		String f = sb.toString();
		if(f.length() > length)
			f = f.substring(0, length);
		return f;
	}
}
