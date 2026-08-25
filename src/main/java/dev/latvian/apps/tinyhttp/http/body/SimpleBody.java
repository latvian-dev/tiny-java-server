package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.ContentTooLargeError;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;
import dev.latvian.apps.tinyhttp.util.ByteBufferUtils;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SimpleBody implements Body {
	private final ByteChannelConnection connection;
	private final long contentLength;
	private final String contentType;

	public SimpleBody(ByteChannelConnection connection, long contentLength, String contentType) {
		this.connection = connection;
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() {
		if (contentLength > Integer.MAX_VALUE) {
			throw new ContentTooLargeError(contentLength, Integer.MAX_VALUE);
		}

		if (contentLength > 0) {
			var byteBuffer = ByteBufferUtils.allocate((int) contentLength, true);

			try {
				connection.read(byteBuffer);
			} catch (IOException ex) {
				throw new UnprocessableContentError("Failed to read the request body", ex);
			}

			return byteBuffer.flip();
		} else {
			return ByteBufferUtils.EMPTY_HEAP;
		}
	}

	@Override
	public void transferTo(OutputStream out) throws IOException {
		connection.read(contentLength, out);
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
	public long contentLength() {
		return contentLength;
	}
}
