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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Utility functions for arrays.
 * 
 * @since 2.2
 */
public final class ArrayUtil {


	private ArrayUtil() {
	}


	/**
	 * Searches for the sequence of bytes given in the <b>seq</b> in the larger <b>arr</b> byte array.
	 * 
	 * @param arr The array in which to search for <b>seq</b>
	 * @param seq The sequence of bytes to search in the larger <b>arr</b> array
	 * @return The index at which the given sequence starts in the <b>arr</b> array, or -1 if the sequence was not found
	 * @deprecated Since 2.7, use the equivalent method {@link #indexOf(byte[], byte[])} instead
	 */
	@Deprecated
	public static int byteArrayIndexOf(byte[] arr, byte[] seq) {
		return indexOf(arr, seq);
	}

	/**
	 * Searches for the sequence of bytes given in the <b>seq</b> in the larger <b>arr</b> byte array.
	 * 
	 * @param arr The array in which to search for <b>seq</b>
	 * @param seq The sequence of bytes to search in the larger <b>arr</b> array
	 * @param start The position to start searching for <b>seq</b>
	 * @return The index at which the given sequence starts in the <b>arr</b> array, or -1 if the sequence was not found
	 * @deprecated Since 2.7, use the equivalent method {@link #indexOf(byte[], byte[], int)} instead
	 */
	@Deprecated
	public static int byteArrayIndexOf(byte[] arr, byte[] seq, int start) {
		return indexOf(arr, seq, start);
	}


	/**
	 * Searches for the <b>sequence</b> of bytes in the larger byte <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(byte[], byte[], int) indexOf}(array, sequence, 0)</code>
	 * </pre>
	 * 
	 * @param array The array in which to search for the <b>sequence</b>
	 * @param sequence The sequence of bytes to search in the larger <b>array</b> array
	 * @return The index at which the given sequence starts in the array, or {@code -1} if the sequence was not found
	 * @since 2.7
	 * @see #indexOf(byte[], byte[], int)
	 * @apiNote This method existed before version 2.7, but was called "byteArrayIndexOf"
	 */
	public static int indexOf(byte[] array, byte[] sequence) {
		return indexOf(array, sequence, 0);
	}

	/**
	 * Searches for the <b>sequence</b> of bytes in the larger byte <b>array</b>, starting at <b>offset</b>.
	 * 
	 * @param array The array in which to search for the <b>sequence</b>
	 * @param sequence The sequence of bytes to search in the larger <b>array</b> array
	 * @param offset The index to start at
	 * @return The index at which the given sequence starts in the array, or {@code -1} if the sequence was not found
	 * @since 2.7
	 * @see #indexOf(byte[], byte[])
	 * @apiNote This method existed before version 2.7, but was called "byteArrayIndexOf"
	 */
	public static int indexOf(byte[] array, byte[] sequence, int offset) {
		for(int i = offset; i < array.length - sequence.length + 1; i++){
			boolean match = true;
			for(int j = 0; j < sequence.length; j++){
				if(array[i + j] != sequence[j]){
					match = false;
					if(j > 1)
						i += j - 1;
					break;
				}
			}
			if(match)
				return i;
		}
		return -1;
	}


	/**
	 * Searches the given {@code byte} <b>b</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(byte[], byte, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param b The {@code byte} value to search in the <b>array</b>
	 * @return The index of the found {@code byte}, or {@code -1} if it was not found
	 * @since 2.7
	 */
	public static int indexOf(byte[] array, byte b) {
		return indexOf(array, b, 0, array.length);
	}

	/**
	 * Searches the given {@code byte} <b>b</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code byte} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param b The {@code byte} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code byte}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.7
	 * @see #indexOf(byte[], byte)
	 */
	public static int indexOf(byte[] array, byte b, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == b)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given {@code char} <b>c</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(char[], char, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param c The {@code char} value to search in the <b>array</b>
	 * @return The index of the found {@code char}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(char[] array, char c) {
		return indexOf(array, c, 0, array.length);
	}

	/**
	 * Searches the given {@code char} <b>c</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code char} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param c The {@code char} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code char}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(char[], char)
	 */
	public static int indexOf(char[] array, char c, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == c)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given {@code short} <b>s</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(short[], short, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param s The {@code short} value to search in the <b>array</b>
	 * @return The index of the found {@code short}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(short[] array, short s) {
		return indexOf(array, s, 0, array.length);
	}

	/**
	 * Searches the given {@code short} <b>s</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code short} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param s The {@code short} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code short}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(short[], short)
	 */
	public static int indexOf(short[] array, short s, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == s)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given {@code int} <b>i</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(int[], int, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param i The {@code int} value to search in the <b>array</b>
	 * @return The index of the found {@code int}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(int[] array, int i) {
		return indexOf(array, i, 0, array.length);
	}

	/**
	 * Searches the given {@code int} <b>i</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code int} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param i The {@code int} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code int}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(int[], int)
	 */
	public static int indexOf(int[] array, int i, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i2 = offset; i2 < offset + length; i2++){
			if(array[i2] == i)
				return i2;
		}
		return -1;
	}

	/**
	 * Searches the given {@code long} <b>l</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(long[], long, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param l The {@code long} value to search in the <b>array</b>
	 * @return The index of the found {@code long}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(long[] array, long l) {
		return indexOf(array, l, 0, array.length);
	}

	/**
	 * Searches the given {@code long} <b>l</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code long} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param l The {@code long} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code long}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(long[], long)
	 */
	public static int indexOf(long[] array, long l, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == l)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given {@code float} <b>f</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(float[], float, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param f The {@code float} value to search in the <b>array</b>
	 * @return The index of the found {@code float}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(float[] array, float f) {
		return indexOf(array, f, 0, array.length);
	}

	/**
	 * Searches the given {@code float} <b>f</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code float} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param f The {@code float} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code float}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(float[], float)
	 */
	public static int indexOf(float[] array, float f, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == f)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given {@code double} <b>d</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(double[], double, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param d The {@code double} value to search in the <b>array</b>
	 * @return The index of the found {@code double}, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static int indexOf(double[] array, double d) {
		return indexOf(array, d, 0, array.length);
	}

	/**
	 * Searches the given {@code double} <b>d</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * If the {@code double} is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array The array to search in
	 * @param d The {@code double} value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found {@code double}, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(double[], double)
	 */
	public static int indexOf(double[] array, double d, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(array[i] == d)
				return i;
		}
		return -1;
	}

	/**
	 * Searches the given element <b>e</b> in the given <b>array</b>.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(Object[], Object, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param <T> The element type
	 * @param array The array to search in
	 * @param e The value to search in the <b>array</b>
	 * @return The index of the found element, or {@code -1} if it was not found
	 * @since 2.8
	 */
	public static <T> int indexOf(T[] array, T e) {
		return indexOf(array, e, 0, array.length);
	}

	/**
	 * Searches the given element <b>e</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> elements.
	 * <p>
	 * Equality is determined using {@link Objects#equals(Object, Object)}. If the element is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param <T> The element type
	 * @param array The array to search in
	 * @param e The value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of elements to search
	 * @return The index of the found element, or {@code -1} if it was not found
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.8
	 * @see #indexOf(Object[], Object)
	 */
	public static <T> int indexOf(T[] array, T e, int offset, int length) {
		checkBounds(array, offset, length);
		for(int i = offset; i < offset + length; i++){
			if(Objects.equals(array[i], e))
				return i;
		}
		return -1;
	}


	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(boolean[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(byte[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(char[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(short[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(int[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(long[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(float[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static void checkBounds(double[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}

	/**
	 * Checks whether <b>offset</b> and <b>length</b> specify a valid set of elements in the <b>array</b>.
	 * 
	 * @param <T> The element type
	 * @param array The array
	 * @param offset The start index
	 * @param length The number of required valid indices starting at <b>offset</b>
	 * @throws IndexOutOfBoundsException If <b>offset</b> is negative or if the end index exceeds the length of the array
	 * @since 2.8
	 */
	public static <T> void checkBounds(T[] array, int offset, int length) {
		if(offset < 0 || offset > array.length - length)
			throw new IndexOutOfBoundsException("offset=" + offset + " length=" + length + " array.length=" + array.length);
	}


	/**
	 * Reads all remaining data from the input stream (<b>is</b>) until end-of-file is reached, the VM runs out of memory or the maximum array size is reached. To limit the amount
	 * of bytes to read, use {@link #readInputStreamToByteArray(InputStream, int)} instead.
	 * 
	 * @param is The input stream
	 * @return The byte array containing all data read from the input stream
	 * @throws IOException If an IO error occurs
	 * @since 2.3
	 */
	public static byte[] readInputStreamToByteArray(InputStream is) throws IOException {
		return readInputStreamToByteArray(is, 0);
	}

	/**
	 * Reads all remaining data from the input stream (<b>is</b>) until end-of-file is reached or the amount of bytes read exceeds the <b>limit</b>.
	 * 
	 * @param is The input stream
	 * @param limit The maximum amount of bytes to read before the read operation is canceled. May be 0 read any amount of bytes
	 * @return The byte array containing all data read from the input stream
	 * @throws IOException If an IO error occurs
	 * @throws IndexOutOfBoundsException If the amount of bytes read exceeds <b>limit</b>
	 * @since 2.3
	 */
	public static byte[] readInputStreamToByteArray(InputStream is, int limit) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while((read = is.read(buffer)) != -1){
			baos.write(buffer, 0, read);
			if(limit > 0 && baos.size() > limit)
				throw new IndexOutOfBoundsException("Maximum size exceeded: " + baos.size() + "/" + limit);
		}
		return baos.toByteArray();
	}


	private static char toHexChar(int v){
		if(v < 10)
			return (char) ('0' + v);
		else if(v < 16)
			return (char) ('a' + v - 10);
		else
			throw new IllegalArgumentException("value " + v);
	}

	/**
	 * Represents the given <b>data</b> as a string of hexadecimal characters.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * <pre><code>
		{@link #toHexString(byte[], int, int) toHexString}(data, 0, data.length)
	 * </code></pre>
	 *
	 * @return The hexadecimal representation of the data
	 * @since 2.11.0
	 */
	public static String toHexString(byte[] data){
		return toHexString(data, 0, data.length);
	}

	/**
	 * Represents the given <b>data</b>, starting at <b>offset</b> and with the given <b>length</b>, as a string of hexadecimal characters.
	 * <p>
	 * Each byte in the byte array slice is represented by exactly two lowercase hexadecimal characters.
	 *
	 * @return The hexadecimal representation of the data
	 * @throws IndexOutOfBoundsException If <b>offset</b> and <b>length</b> refer to an index outside of the given array
	 * @since 2.11.0
	 */
	public static String toHexString(byte[] data, int offset, int length){
		checkBounds(data, offset, length);
		char[] str = new char[length << 1];
		for(int i = 0; i < length; i++){
			byte b = data[offset + i];
			str[i << 1] = toHexChar((b >> 4) & 0xf);
			str[(i << 1) + 1] = toHexChar(b & 0xf);
		}
		return new String(str);
	}
}
