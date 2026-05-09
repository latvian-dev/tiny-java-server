package dev.latvian.apps.tinyhttp;

public record StatusCode(int code, String message) {
	@Override
	public String toString() {
		return code + " " + message;
	}

	public StatusCode withMessage(String message) {
		return new StatusCode(code, message);
	}
}
