package dev.latvian.apps.tinyserver.http.body;

import java.nio.ByteBuffer;

public record SimpleBody(ByteBuffer byteBuffer, String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() {
		return byteBuffer.position(0);
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public String toString() {
		return "body";
	}
}
