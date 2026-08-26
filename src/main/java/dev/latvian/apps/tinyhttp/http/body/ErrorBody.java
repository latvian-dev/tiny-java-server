package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.HTTPError;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public record ErrorBody(String errorType, Supplier<HTTPError> error) implements Body {
	@Override
	public ByteBuffer byteBuffer() {
		throw error.get();
	}

	@Override
	public long contentLength() {
		throw error.get();
	}

	@Override
	public String toString() {
		return errorType;
	}
}
