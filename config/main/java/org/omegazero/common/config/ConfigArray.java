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
package org.omegazero.common.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A read-only array of any type of data, providing a layer of abstraction for different data representations. Created by a {@link Configuration} instance.
 * 
 * @since 2.4
 */
public class ConfigArray implements Serializable, Iterable<Object> {

	private static final long serialVersionUID = 1L;


	/**
	 * The data.
	 */
	protected final List<Object> data;

	/**
	 * Creates an empty {@code ConfigArray}.
	 */
	public ConfigArray() {
		this(null, false);
	}

	/**
	 * Creates a {@code ConfigArray}.
	 * 
	 * @param initialCapacity The number of elements
	 */
	@Deprecated
	public ConfigArray(int initialCapacity) {
		this.data = new ArrayList<>(initialCapacity);
	}

	/**
	 * Creates a {@code ConfigArray} with the given elements.
	 * <p>
	 * Changes on the given list have no effects on this {@code ConfigArray}.
	 * 
	 * @param data The data
	 */
	public ConfigArray(List<Object> data) {
		this(data, true);
	}

	private ConfigArray(List<Object> data, boolean copy) {
		if(data == null)
			this.data = java.util.Collections.emptyList();
		else
			this.data = copy ? new ArrayList<>(data) : data;
	}


	/**
	 * Creates a new {@link ArrayList} with all values of this <code>ConfigArray</code>. Changes to the returned list have no effect on this <code>ConfigArray</code>.
	 * 
	 * @return A <code>List</code> with all values of this <code>ConfigArray</code>
	 */
	public List<Object> copyData() {
		return new ArrayList<>(this.data);
	}


	/**
	 * Creates a new <code>ConfigArray</code> that contains all values of this and the given <code>ConfigArray</code>. Values of this <code>ConfigArray</code> are added first,
	 * followed by values in the provided <code>ConfigArray</code>.<br>
	 * Both provided objects stay unchanged.
	 * 
	 * @param other The <code>ConfigArray</code> to merge this one with
	 * @return A new <code>ConfigArray</code> with all values of this and <b>other</b>
	 */
	public ConfigArray merge(ConfigArray other) {
		List<Object> newData = new ArrayList<>(this.data.size() + other.data.size());
		newData.addAll(this.data);
		newData.addAll(other.data);
		return new ConfigArray(newData, false);
	}


	/**
	 * Returns the number of elements in this {@code ConfigArray}.
	 * 
	 * @return The number of elements
	 * @see List#size()
	 */
	public int size() {
		return this.data.size();
	}

	/**
	 * Returns whether this {@code ConfigArray} is empty.
	 * 
	 * @return {@code true} if empty
	 * @see List#isEmpty()
	 */
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	/**
	 * Returns whether the given element <b>o</b> is in this {@code ConfigArray}.
	 * 
	 * @param o The element to search
	 * @return {@code true} if <b>o</b> exists
	 * @see List#contains(Object)
	 */
	public boolean contains(Object o) {
		return this.data.contains(o);
	}

	/**
	 * Converts this {@code ConfigArray} to an {@code Object} array.
	 * 
	 * @return The array containing all elements
	 * @see #toArray(Class)
	 * @see List#toArray()
	 */
	public Object[] toArray() {
		return this.data.toArray();
	}

	/**
	 * Converts this {@code ConfigArray} to an array of the given <b>type</b>.
	 *
	 * @param type The target type
	 * @return The array containing all elements
	 * @throws ClassCastException If any element in this {@code ConfigArray} is not of the given <b>type</b>
	 * @since 2.11.0
	 * @see #toArray()
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(Class<T> type){
		T[] array = (T[]) java.lang.reflect.Array.newInstance(type, this.size());
		for(int i = 0; i < array.length; i++){
			Object o = this.get(i);
			if(!type.isAssignableFrom(o.getClass()))
				throw new ClassCastException("Cannot convert array to element type " + type.getName() + ": " + o.getClass() + " cannot be cast to " + type);
			array[i] = (T) o;
		}
		return array;
	}

	/**
	 * Returns whether all elements in the given {@link Collection} are contained in this {@code ConfigArray}.
	 * 
	 * @param c The collection
	 * @return {@code true} if all elements of the collection exist
	 * @see List#containsAll(Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return this.data.containsAll(c);
	}

	/**
	 * Returns the element at the given position in this {@code ConfigArray}.
	 * 
	 * @param index The index
	 * @return The element at the index
	 * @see List#get(int)
	 */
	public Object get(int index) {
		return this.data.get(index);
	}

	/**
	 * Returns the index of the given element.
	 * 
	 * @param o The element to search
	 * @return The index of the element, or {@code -1} if the element does not exist
	 * @see List#indexOf(Object)
	 */
	public int indexOf(Object o) {
		return this.data.indexOf(o);
	}

	/**
	 * Returns the last index of the given element.
	 * 
	 * @param o The element to search
	 * @return The last index of the element, or {@code -1} if the element does not exist
	 * @see List#lastIndexOf(Object)
	 */
	public int lastIndexOf(Object o) {
		return this.data.lastIndexOf(o);
	}


	@Override
	public Iterator<Object> iterator() {
		return new Iterator<Object>(){

			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < ConfigArray.this.size();
			}

			@Override
			public Object next() {
				return ConfigArray.this.get(this.index++);
			}
		};
	}


	/**
	 * Determines whether the given object is equal to this {@code ConfigArray}.
	 * <p>
	 * Another object is equal if it is also a {@code ConfigArray} and contains the same elements.
	 * 
	 * @see List#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ConfigArray))
			return false;
		return ((ConfigArray) o).data.equals(this.data);
	}

	/**
	 * Returns a hash code for this {@code ConfigArray}.
	 * 
	 * @see List#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.data.hashCode();
	}

	/**
	 * Returns a clone of this {@code ConfigArray}, which contains the same elements as this {@code ConfigArray}.
	 */
	@Override
	public ConfigArray clone() {
		return new ConfigArray(this.data, true);
	}

	/**
	 * Returns a string representation of this {@code ConfigArray}.
	 * 
	 * @see List#toString()
	 */
	@Override
	public String toString() {
		return this.data.toString();
	}
}
