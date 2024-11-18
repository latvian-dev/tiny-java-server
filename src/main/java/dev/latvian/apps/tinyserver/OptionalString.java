package dev.latvian.apps.tinyserver;

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
}
