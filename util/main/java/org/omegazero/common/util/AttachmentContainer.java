/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util;

/**
 * Represents an object that can hold attachments (any {@code Object}s), each identified by a unique name string.
 * 
 * @since 2.9
 * @see SimpleAttachmentContainer
 */
public interface AttachmentContainer {


	/**
	 * Retrieves an attachment identified by the given <b>key</b> previously set by {@link #setAttachment(String, Object)}. If no attribute was set with the given <b>key</b>,
	 * {@code null} is returned.
	 * 
	 * @param key The name of the attachment
	 * @return The value of the attachment, or {@code null} if no attachment with the given <b>key</b> exists
	 */
	public Object getAttachment(String key);

	/**
	 * Stores an object identified by the given <b>key</b>.
	 * <p>
	 * Values stored here likely have no meaning in the used application and are purely intended to store metadata used by the application.
	 * 
	 * @param key The name string identifying the given <b>value</b> in this {@code AttributeContainer}
	 * @param value The value to be stored, or {@code null} to delete an existing attachment
	 */
	public void setAttachment(String key, Object value);

	/**
	 * Retrieves an attachment identified by the given <b>key</b> previously set by {@link #setAttachment(String, Object)}. If no attribute was set with the given <b>key</b>, a
	 * {@code NoSuchElementException} is thrown.
	 * 
	 * @param key The name of the attachment
	 * @return The value of the attachment
	 * @see #hasAttachment(String)
	 */
	public Object requireAttachment(String key);

	/**
	 * Determines whether an attachment identified by the given <b>key</b> exists.
	 * 
	 * @param key The name of the attachment
	 * @return {@code true} if an attachment with the given <b>key</b> exists
	 * @see #requireAttachment(String)
	 */
	public boolean hasAttachment(String key);

	/**
	 * Removes an attachment identified by the given <b>key</b> and returns it.
	 * 
	 * @param key The name of the attachment
	 * @return The value of the attachment, or {@code null} if no attachment with the given <b>key</b> existed
	 */
	public Object removeAttachment(String key);
}
