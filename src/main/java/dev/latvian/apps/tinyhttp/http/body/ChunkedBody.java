package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class ChunkedBody implements Body {
	private final ByteChannelConnection connection;
	private final String contentType;
	private final int sizeHint;
	private ByteBuffer byteBuffer;

	public ChunkedBody(ByteChannelConnection connection, String contentType, int sizeHint) {
		this.connection = connection;
		this.contentType = contentType;
		this.sizeHint = sizeHint;
	}

	@Override
	public ByteBuffer byteBuffer() throws IOException {
		if (byteBuffer == null || !byteBuffer.hasRemaining()) {
			byteBuffer = nextByteBuffer();
		}

		return byteBuffer;
	}

	private ByteBuffer nextByteBuffer() throws IOException {
		var chunkHeader = connection.readCRLF();
		int chunkSize = Integer.parseUnsignedInt(chunkHeader.split(";", 2)[0], 16);
		var bytes = new ByteArrayOutputStream(sizeHint > 0 ? sizeHint : chunkSize);

		while (chunkSize > 0) {
			var tempBuf = ByteBuffer.allocate(chunkSize);
			connection.read(tempBuf);
			bytes.write(tempBuf.array());
			var dataEnd = connection.readCRLF();

			if (!dataEnd.isEmpty()) {
				throw new BadRequestError("Expected CRLF after chunk data, got '" + dataEnd + "'");
			}

			chunkHeader = connection.readCRLF();
			chunkSize = Integer.parseUnsignedInt(chunkHeader.split(";", 2)[0], 16);
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
