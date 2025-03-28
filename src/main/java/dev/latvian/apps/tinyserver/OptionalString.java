package dev.latvian.apps.tinyserver;

import dev.latvian.apps.tinyserver.http.response.error.client.BadRequestError;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Function;

public record OptionalString(String value) {
	public static final OptionalString MISSING = new OptionalString(null);
	public static final OptionalString EMPTY = new OptionalString("");

	public static OptionalString of(String str) {
		return str == null ? MISSING : str.isEmpty() ? EMPTY : new OptionalString(str);
	}

	public boolean isMissing() {
		return value == null;
	}

	public boolean isPresent() {
		return value != null;
	}

	public OptionalString require() {
		if (value == null) {
			throw new BadRequestError("Required value is missing!");
		}

		return this;
	}

	@Override
	public String toString() {
		return value == null ? "<empty>" : value;
	}

	public String asString() {
		return value == null ? "" : value;
	}

	public String asString(String def) {
		return value == null ? def : value;
	}

	public <T> T as(Function<String, T> mapper, T def) {
		if (value == null) {
			return def;
		}

		try {
			return mapper.apply(value);
		} catch (Throwable ex) {
			return def;
		}
	}

	public <T> T as(Function<String, T> mapper) {
		return as(mapper, null);
	}

	public boolean asBoolean() {
		return asBoolean(false);
	}

	public boolean asBoolean(boolean def) {
		if (value == null) {
			return def;
		}

		return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1");
	}

	public int asInt() {
		return asInt(0);
	}

	public int asInt(int def) {
		if (value == null) {
			return def;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public long asLong() {
		return asLong(0L);
	}

	public long asLong(long def) {
		if (value == null) {
			return def;
		}

		try {
			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public long asULong() {
		return asULong(0L);
	}

	public long asULong(long def) {
		if (value == null) {
			return def;
		}

		try {
			return Long.parseUnsignedLong(value);
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public float asFloat() {
		return asFloat(0F);
	}

	public float asFloat(float def) {
		if (value == null) {
			return def;
		}

		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public double asDouble() {
		return asDouble(0D);
	}

	public double asDouble(double def) {
		if (value == null) {
			return def;
		}

		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public ZoneId asZoneId() {
		if (value == null) {
			return ZoneOffset.UTC;
		}

		var string = asString("UTC");

		if (string.isEmpty() || string.equals("UTC")) {
			return ZoneOffset.UTC;
		} else if (string.startsWith("GMT")) {
			string = "Etc/" + string;
		}

		try {
			return ZoneId.of(string);
		} catch (Exception ignore) {
		}

		var l = string.toLowerCase();
		var allZones = ZoneId.getAvailableZoneIds();

		try {
			for (var s : allZones) {
				if (s.toLowerCase().equals(l)) {
					return ZoneId.of(s);
				}
			}
		} catch (Exception ignore) {
		}

		try {
			for (var s : allZones) {
				if (s.toLowerCase().contains(l)) {
					return ZoneId.of(s);
				}
			}
		} catch (Exception ignore) {
		}

		throw new IllegalArgumentException("Unknown zone ID!");
	}
}
