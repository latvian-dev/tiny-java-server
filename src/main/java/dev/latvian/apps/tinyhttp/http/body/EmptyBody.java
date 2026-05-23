package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.OptionalString;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

public record EmptyBody(String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() {
		return ByteBuffer.allocate(0);
	}

	@Override
	public long transferTo(OutputStream out) {
		return 0L;
	}

	@Override
	public String text() {
		return "";
	}

	@Override
	public byte[] bytes() {
		return new byte[0];
	}

	@Override
	public Map<String, OptionalString> getPostData() {
		return Map.of();
	}

	@Override
	public String toString() {
		return "empty_body";
	}
}
