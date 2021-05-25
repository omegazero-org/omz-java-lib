package org.omegazero.common.plugins;

public class InvalidPluginException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidPluginException(String name, String msg) {
		super("Plugin '" + name + "': " + msg);
	}
}
