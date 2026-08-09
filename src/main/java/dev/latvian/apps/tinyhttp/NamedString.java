package dev.latvian.apps.tinyhttp;

import org.jetbrains.annotations.NotNull;

public record NamedString(String name, OptionalString value) {
	public static NamedString of(String name, String value) {
		return new NamedString(name, OptionalString.of(value));
	}

	public static NamedString empty(String name) {
		return new NamedString(name, OptionalString.EMPTY);
	}

	public boolean is(String name) {
		return this.name.equalsIgnoreCase(name);
	}

	@Override
	public @NotNull String toString() {
		var str = value.asString();
		return str.isEmpty() ? name : (name + "=" + str);
	}
}
