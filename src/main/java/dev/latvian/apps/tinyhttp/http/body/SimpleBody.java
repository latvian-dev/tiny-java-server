package dev.latvian.apps.tinyhttp.http.body;

import java.nio.ByteBuffer;

public record SimpleBody(ByteBuffer byteBuffer, int contentLength, String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() {
		return byteBuffer.position(0);
	}

	@Override
	public String toString() {
		return "body";
	}
}
