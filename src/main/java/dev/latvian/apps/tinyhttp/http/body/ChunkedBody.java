package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class ChunkedBody implements Body {
	private final ByteChannelConnection connection;
	private final String contentType;
	private ByteBuffer byteBuffer;

	public ChunkedBody(ByteChannelConnection connection, String contentType) {
		this.connection = connection;
		this.contentType = contentType;
	}

	@Override
	public ByteBuffer byteBuffer() throws IOException {
		if (byteBuffer == null || !byteBuffer.hasRemaining()) {
			byteBuffer = nextByteBuffer();
		}

		return byteBuffer;
	}

	private ByteBuffer nextByteBuffer() throws IOException {
		int chunkSize = Integer.parseUnsignedInt(connection.readCRLF().split(";", 2)[0], 16);
		var bytes = new ByteArrayOutputStream();

		while (chunkSize > 0) {
			connection.read(bytes, chunkSize);

			var dataEnd = connection.readCRLF();

			if (!dataEnd.isEmpty()) {
				throw new BadRequestError("Expected CRLF after chunk data, got '" + dataEnd + "'");
			}

			chunkSize = Integer.parseUnsignedInt(connection.readCRLF().split(";", 2)[0], 16);
		}

		var dataEnd = connection.readCRLF();

		if (!dataEnd.isEmpty()) {
			throw new BadRequestError("Expected CRLF after final chunk data, got '" + dataEnd + "'");
		}

		return ByteBuffer.wrap(bytes.toByteArray());
	}

	public ByteChannelConnection connection() {
		return connection;
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public String toString() {
		return "chunked_body:" + contentType;
	}
}
