package dev.latvian.apps.tinyserver.http;

import java.util.Date;

@FunctionalInterface
public interface HeaderConsumer {
	void addHeader(Header header);

	default void addHeader(String header, Object value) {
		addHeader(new Header(header, String.valueOf(value)));
	}

	default void addUnsignedHeader(String header, int value) {
		addHeader(header, Integer.toUnsignedString(value));
	}

	default void addUnsignedHeader(String header, long value) {
		addHeader(header, Long.toUnsignedString(value));
	}

	default void addDateHeader(String header, Date value) {
		addHeader(new Header(header, String.valueOf(value)));
	}
}
