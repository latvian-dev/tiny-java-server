package dev.latvian.apps.tinyhttp.error;

public class InvalidPathException extends RuntimeException {
	public InvalidPathException(String path) {
		super("Invalid path: " + path);
	}
}
