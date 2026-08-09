package dev.latvian.apps.tinyhttp.http.body;

import java.nio.ByteBuffer;

public final class ByteBody implements Body {
	private final ByteBuffer byteBuffer;
	private final String contentType;

	public ByteBody(ByteBuffer byteBuffer, String contentType) {
		this.byteBuffer = byteBuffer;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() {
		return byteBuffer.clear();
	}

	@Override
	public String toString() {
		return "byte_body:" + contentType;
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public int contentLength() {
		return byteBuffer.limit();
	}
}
