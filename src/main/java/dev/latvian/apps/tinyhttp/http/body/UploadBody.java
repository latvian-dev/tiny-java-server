package dev.latvian.apps.tinyhttp.http.body;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class UploadBody implements Body {
	private final byte[] bytes;
	private final String contentType;
	private ByteBuffer byteBuffer;

	public UploadBody(byte[] bytes, String contentType) {
		this.bytes = bytes;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() {
		if (byteBuffer == null) {
			byteBuffer = ByteBuffer.wrap(bytes);
			return byteBuffer;
		} else {
			return byteBuffer.clear();
		}
	}

	@Override
	public String text() {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public byte[] bytes() {
		return bytes;
	}

	@Override
	public String toString() {
		return "upload_body:" + contentType + "; " + bytes.length + " bytes";
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public int contentLength() {
		return bytes.length;
	}
}
