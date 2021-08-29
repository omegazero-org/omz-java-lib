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


	protected final List<Object> data;

	public ConfigArray() {
		this(new ArrayList<>());
	}

	public ConfigArray(int initialCapacity) {
		this(new ArrayList<>(initialCapacity));
	}

	public ConfigArray(List<Object> data) {
		if(data == null)
			throw new NullPointerException("data is null");
		this.data = data;
	}


	/**
	 * Creates a new {@link ArrayList} with all values of this <code>ConfigArray</code>. Changes to the returned list have no effect on this <code>ConfigArray</code>.
	 * 
	 * @return A <code>List</code> with all values of this <code>ConfigArray</code>
	 */
	public List<Object> copyData() {
		return new ArrayList<>(this.data);
	}


	public int size() {
		return this.data.size();
	}

	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	public boolean contains(Object o) {
		return this.data.contains(o);
	}

	public Object[] toArray() {
		return this.data.toArray();
	}

	public boolean containsAll(Collection<?> c) {
		return this.data.containsAll(c);
	}

	public Object get(int index) {
		return this.data.get(index);
	}

	public int indexOf(Object o) {
		return this.data.indexOf(o);
	}

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


	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ConfigArray))
			return false;
		return ((ConfigArray) o).data.equals(this.data);
	}

	@Override
	public ConfigArray clone() {
		return new ConfigArray(this.copyData());
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
}
