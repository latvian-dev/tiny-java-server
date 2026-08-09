package dev.latvian.apps.tinyhttp.http.body;

import java.nio.ByteBuffer;

public final class UploadBody implements Body {
	private final String content;
	private final ByteBuffer byteBuffer;
	private final String contentType;

	public UploadBody(String content, ByteBuffer byteBuffer, String contentType) {
		this.content = content;
		this.byteBuffer = byteBuffer;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() {
		return byteBuffer.clear();
	}

	@Override
	public String text() {
		return content;
	}

	@Override
	public String toString() {
		return "upload_body:" + contentType;
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
