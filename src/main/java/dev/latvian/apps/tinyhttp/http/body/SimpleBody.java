package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleBody implements Body {
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
	public ByteBuffer byteBuffer() {
		if (byteBuffer == null) {
			byteBuffer = ByteBufferUtils.allocate(contentLength, false);

			if (contentLength > 0) {
				try {
					connection.read(byteBuffer);
				} catch (IOException ex) {
					throw new UnprocessableContentError("Failed to read the request body", ex);
				}
			}

			return byteBuffer.flip();
		} else {
			return byteBuffer.clear();
		}
	}

	@Override
	public String toString() {
		return "simple_body:" + contentType + "; " + contentLength() + " bytes";
	}

	public ByteChannelConnection connection() {
		return connection;
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public int contentLength() {
		return contentLength;
	}
}
