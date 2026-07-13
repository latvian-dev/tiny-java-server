package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class SimpleBody implements Body {
	private final ByteChannelConnection connection;
	private final int contentLength;
	private final String contentType;
	private ByteBuffer byteBuffer;

	public SimpleBody(ByteChannelConnection connection, int contentLength, String contentType) {
		this.connection = connection;
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() throws IOException {
		if (byteBuffer == null) {
			byteBuffer = nextByteBuffer();
		}

		return byteBuffer;
	}

	public ByteBuffer nextByteBuffer() throws IOException {
		var bodyBuffer = ByteBuffer.allocate(contentLength);

		if (contentLength > 0) {
			connection.read(bodyBuffer);
		}

		return bodyBuffer.flip();
	}

	@Override
	public String toString() {
		return "simple_body:" + contentType;
	}

	public ByteChannelConnection connection() {
		return connection;
	}

	public int contentLength() {
		return contentLength;
	}

	@Override
	public String contentType() {
		return contentType;
	}
}
