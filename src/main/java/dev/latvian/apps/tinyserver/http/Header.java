package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.OptionalString;

public record Header(String key, OptionalString value) {
	public boolean is(String name) {
		return key.equalsIgnoreCase(name);
	}

	public Header(String key, String value) {
		this(key, OptionalString.of(value));
	}
}
