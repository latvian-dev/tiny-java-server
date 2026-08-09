package dev.latvian.apps.tinyhttp.http;

import dev.latvian.apps.tinyhttp.NamedString;

import java.util.Date;

@FunctionalInterface
public interface HeaderConsumer {
	void addHeader(NamedString header);

	default void addHeader(String header, Object value) {
		addHeader(NamedString.of(header, String.valueOf(value)));
	}

	default void addUnsignedHeader(String header, int value) {
		addHeader(header, Integer.toUnsignedString(value));
	}

	default void addUnsignedHeader(String header, long value) {
		addHeader(header, Long.toUnsignedString(value));
	}

	default void addDateHeader(String header, Date value) {
		addHeader(NamedString.of(header, String.valueOf(value)));
	}
}
