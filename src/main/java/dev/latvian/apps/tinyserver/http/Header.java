package dev.latvian.apps.tinyserver.http;

public record Header(String key, String value) {
	public boolean is(String name) {
		return key.equalsIgnoreCase(name);
	}
}
