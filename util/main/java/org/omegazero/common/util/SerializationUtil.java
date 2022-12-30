/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility functions for object serialization.
 *
 * @since 2.11.0
 */
public final class SerializationUtil {


	private SerializationUtil() {
	}


	/**
	 * Serializes the given <b>object</b> into a byte array using an {@link ObjectOutputStream}.
	 *
	 * @param object The object
	 * @return The serialized object data
	 * @throws IOException If serialization fails (for example because the object is not serializable)
	 */
	public static byte[] serialize(Object object) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
		objStream.writeObject(object);
		objStream.close();
		return byteStream.toByteArray();
	}

	/**
	 * Deserializes the given object <b>data</b> using an {@link ObjectInputStream}.
	 *
	 * @param data The serialized object data
	 * @return The deserialized object
	 * @throws IOException If deserialization fails (for example because a serialized class is not found)
	 */
	public static Object deserialize(byte[] data) throws IOException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		ObjectInputStream objStream = new ObjectInputStream(byteStream);
		Object obj;
		try{
			obj = objStream.readObject();
		}catch(ClassNotFoundException e){
			throw new IOException("Deserialization failed: " + e, e);
		}
		objStream.close();
		return obj;
	}
}
