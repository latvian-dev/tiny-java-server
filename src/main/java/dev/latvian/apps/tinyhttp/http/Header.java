package dev.latvian.apps.tinyhttp.http;

import dev.latvian.apps.tinyhttp.OptionalString;

public record Header(String key, OptionalString value) {
	public boolean is(String name) {
		return key.equalsIgnoreCase(name);
	}

	public Header(String key, String value) {
		this(key, OptionalString.of(value));
	}
}
