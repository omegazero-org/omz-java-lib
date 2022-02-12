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
	 * @param arr   The array in which to search for <b>seq</b>
	 * @param seq   The sequence of bytes to search in the larger <b>arr</b> array
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
	 * @param array    The array in which to search for the <b>sequence</b>
	 * @param sequence The sequence of bytes to search in the larger <b>array</b> array
	 * @return The index at which the given sequence starts in the array, or {@code -1} if the sequence was not found
	 * @since 2.7
	 * @see #indexOf(byte[], byte[], int)
	 * @apiNote This method has existed before version 2.7, but was called "byteArrayIndexOf"
	 */
	public static int indexOf(byte[] array, byte[] sequence) {
		return indexOf(array, sequence, 0);
	}

	/**
	 * Searches for the <b>sequence</b> of bytes in the larger byte <b>array</b>, starting at <b>offset</b>.
	 * 
	 * @param array    The array in which to search for the <b>sequence</b>
	 * @param sequence The sequence of bytes to search in the larger <b>array</b> array
	 * @param offset   The index to start at
	 * @return The index at which the given sequence starts in the array, or {@code -1} if the sequence was not found
	 * @since 2.7
	 * @see #indexOf(byte[], byte[])
	 * @apiNote This method has existed before version 2.7, but was called "byteArrayIndexOf"
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
	 * Searches the given byte <b>b</b> in the given <b>array</b>.
	 * <p>
	 * If the byte is not found within the array, {@code -1} is returned.
	 * <p>
	 * A call to this method is equivalent to a call to
	 * 
	 * <pre>
	 * <code>{@link #indexOf(byte[], byte, int, int) indexOf}(array, b, 0, array.length)</code>
	 * </pre>
	 * 
	 * @param array The array to search in
	 * @param b     The byte value to search in the <b>array</b>
	 * @return The index of the found byte, or {@code -1} if the byte was not found
	 * @since 2.7
	 * @see #indexOf(byte[], byte, int, int)
	 */
	public static int indexOf(byte[] array, byte b) {
		return indexOf(array, b, 0, array.length);
	}

	/**
	 * Searches the given byte <b>b</b> in the given <b>array</b>, starting at <b>offset</b> and continuing for a maximum of <b>length</b> bytes.
	 * <p>
	 * If the byte is not found within the given bounds, {@code -1} is returned.
	 * 
	 * @param array  The array to search in
	 * @param b      The byte value to search in the <b>array</b>
	 * @param offset The index to start at
	 * @param length The maximum number of bytes to search
	 * @return The index of the found byte, or {@code -1} if the byte was not found
	 * @throws IllegalArgumentException If <b>offset</b> is negative or if the end index of the search would exceed the length of the array
	 * @since 2.7
	 * @see #indexOf(byte[], byte)
	 */
	public static int indexOf(byte[] array, byte b, int offset, int length) {
		if(offset < 0)
			throw new IllegalArgumentException("offset: " + offset);
		int end = offset + length;
		if(end > array.length)
			throw new IllegalArgumentException("end: " + end + " array.length: " + array.length);
		for(int i = offset; i < end; i++){
			if(array[i] == b)
				return i;
		}
		return -1;
	}


	/**
	 * Reads all remaining data from the input stream (<b>is</b>) until end-of-file is reached, the VM runs out of memory or the maximum array size is reached. To limit the
	 * amount of bytes to read, use {@link #readInputStreamToByteArray(InputStream, int)} instead.
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
	 * @param is    The input stream
	 * @param limit The maximum amount of bytes to read before the read operation is canceled. May be 0 read any amount of bytes
	 * @return The byte array containing all data read from the input stream
	 * @throws IOException               If an IO error occurs
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
}
