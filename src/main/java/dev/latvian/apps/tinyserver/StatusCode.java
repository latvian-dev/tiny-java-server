package dev.latvian.apps.tinyserver;

public record StatusCode(int code, String message) {
	@Override
	public String toString() {
		return code + " " + message;
	}
}
