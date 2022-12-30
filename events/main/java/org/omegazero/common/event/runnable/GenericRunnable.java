/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.event.runnable;

/**
 * Represents a runnable or method reference with any number of arguments with any type, allowed to throw any exception.
 *
 * @since 2.10
 */
public interface GenericRunnable {

	/**
	 * Runs this {@code GenericRunnable}.
	 *
	 * @param args The arguments
	 * @throws IllegalArgumentException If the number of elements in {@code args} does not match the value returned by {@link #getArgumentCount()}
	 * @throws Exception Any exception thrown by the runnable
	 */
	public void run(Object... args) throws Exception;

	/**
	 * Returns the number of arguments this {@code GenericRunnable} accepts.
	 *
	 * @return The number of arguments
	 */
	public int getArgumentCount();


	/**
	 * Checks whether {@code count} is the correct number of arguments for this {@code GenericRunnable}.
	 *
	 * @param count
	 * @throws IllegalArgumentException If {@code count} does not match the value returned by {@link #getArgumentCount()}
	 */
	public default void checkArgc(int count){
		if(count != this.getArgumentCount())
			throw new IllegalArgumentException("Incorrect number of arguments (runnable has: " + this.getArgumentCount() + ", got: " + count + ")");
	}


	/**
	 * A {@link GenericRunnable} with 0 arguments.
	 *
	 * @since 2.10
	 */
	@FunctionalInterface
	public static interface A0 extends GenericRunnable {

		/**
		 * Runs this {@code GenericRunnable}.
		 */
		public void run() throws Exception;


		@Override
		public default void run(Object... args) throws Exception {
			this.checkArgc(args.length);
			this.run();
		}

		@Override
		public default int getArgumentCount(){
			return 0;
		}
	}

	/**
	 * A {@link GenericRunnable} with 1 argument.
	 *
	 * @param <A> The type of the first argument
	 * @since 2.10
	 */
	@FunctionalInterface
	public interface A1<A> extends GenericRunnable {

		/**
		 * Runs this {@code GenericRunnable} with the given argument.
		 *
		 * @param arg0 The first argument
		 */
		public void run(A arg0) throws Exception;


		@Override
		@SuppressWarnings("unchecked")
		public default void run(Object... args) throws Exception {
			this.checkArgc(args.length);
			this.run((A) args[0]);
		}

		@Override
		public default int getArgumentCount(){
			return 1;
		}
	}

	/**
	 * A {@link GenericRunnable} with 2 arguments.
	 *
	 * @param <A> The type of the first argument
	 * @param <B> The type of the second argument
	 * @since 2.10
	 */
	@FunctionalInterface
	public interface A2<A, B> extends GenericRunnable {

		/**
		 * Runs this {@code A2} with the given arguments.
		 *
		 * @param arg0 The first argument
		 * @param arg1 The second argument
		 */
		public void run(A arg0, B arg1) throws Exception;


		@Override
		@SuppressWarnings("unchecked")
		public default void run(Object... args) throws Exception {
			this.checkArgc(args.length);
			this.run((A) args[0], (B) args[1]);
		}

		@Override
		public default int getArgumentCount(){
			return 2;
		}
	}

	/**
	 * A {@link GenericRunnable} with 3 arguments.
	 *
	 * @param <A> The type of the first argument
	 * @param <B> The type of the second argument
	 * @param <C> The type of the third argument
	 * @since 2.10
	 */
	@FunctionalInterface
	public interface A3<A, B, C> extends GenericRunnable {

		/**
		 * Runs this {@code A3} with the given arguments.
		 *
		 * @param arg0 The first argument
		 * @param arg1 The second argument
		 * @param arg2 The third argument
		 */
		public void run(A arg0, B arg1, C arg2) throws Exception;


		@Override
		@SuppressWarnings("unchecked")
		public default void run(Object... args) throws Exception {
			this.checkArgc(args.length);
			this.run((A) args[0], (B) args[1], (C) args[2]);
		}

		@Override
		public default int getArgumentCount(){
			return 3;
		}
	}

	/**
	 * A {@link GenericRunnable} with 4 arguments.
	 *
	 * @param <A> The type of the first argument
	 * @param <B> The type of the second argument
	 * @param <C> The type of the third argument
	 * @param <D> The type of the fourth argument
	 * @since 2.10
	 */
	@FunctionalInterface
	public interface A4<A, B, C, D> extends GenericRunnable {

		/**
		 * Runs this {@code A4} with the given arguments.
		 *
		 * @param arg0 The first argument
		 * @param arg1 The second argument
		 * @param arg2 The third argument
		 * @param arg3 The fourth argument
		 */
		public void run(A arg0, B arg1, C arg2, D arg3) throws Exception;


		@Override
		@SuppressWarnings("unchecked")
		public default void run(Object... args) throws Exception {
			this.checkArgc(args.length);
			this.run((A) args[0], (B) args[1], (C) args[2], (D) args[3]);
		}

		@Override
		public default int getArgumentCount(){
			return 4;
		}
	}
}
