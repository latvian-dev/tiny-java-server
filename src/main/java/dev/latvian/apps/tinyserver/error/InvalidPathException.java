package dev.latvian.apps.tinyserver.error;

public class InvalidPathException extends RuntimeException {
	public InvalidPathException(String path) {
		super("Invalid path: " + path);
	}
}
