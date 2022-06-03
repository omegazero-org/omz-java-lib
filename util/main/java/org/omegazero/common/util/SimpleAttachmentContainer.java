/*
 * Copyright (C) 2022 omegazero.org, user94729
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.omegazero.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An {@link AttachmentContainer} implementation using a {@link HashMap}, which may be used as a superclass for more specific objects.
 * <p>
 * This class is not thread-safe.
 * 
 * @since 2.9
 */
public class SimpleAttachmentContainer implements AttachmentContainer {


	/**
	 * Map containing the values of this {@code SimpleAttachmentContainer}.
	 */
	protected Map<String, Object> attachments = null;


	@Override
	public Object getAttachment(String key) {
		if(this.attachments == null)
			return null;
		else
			return this.attachments.get(key);
	}

	@Override
	public void setAttachment(String key, Object value) {
		if(this.attachments == null)
			this.attachments = new HashMap<>();
		if(value != null)
			this.attachments.put(key, value);
		else
			this.attachments.remove(key);
	}

	@Override
	public Object requireAttachment(String key) {
		Object v = this.getAttachment(key);
		if(v != null)
			return v;
		else
			throw new NoSuchElementException("No attribute named '" + key + "'");
	}

	@Override
	public boolean hasAttachment(String key) {
		return this.attachments != null && this.attachments.containsKey(key);
	}

	@Override
	public Object removeAttachment(String key) {
		if(this.attachments == null)
			return null;
		else
			return this.attachments.remove(key);
	}
}
