package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.apps.tinyhttp.http.response.error.client.ContentTooLargeError;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;
import dev.latvian.apps.tinyhttp.http.response.error.server.InternalError;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class ChunkedBody implements Body {
	private final ByteChannelConnection connection;
	private final String contentType;
	private final long sizeHint;

	public ChunkedBody(ByteChannelConnection connection, String contentType, long sizeHint) {
		this.connection = connection;
		this.contentType = contentType;
		this.sizeHint = sizeHint;
	}

	@Override
	public ByteBuffer byteBuffer() {
		return ByteBuffer.wrap(bytes());
	}

	@Override
	public long contentLength() {
		if (sizeHint > 0L) {
			return sizeHint;
		} else {
			throw new InternalError("Chunked body content length is unknown");
		}
	}

	@Override
	public void transferTo(OutputStream out) throws IOException {
		var chunkHeader = connection.readCRLF();
		int chunkSize = Integer.parseUnsignedInt(chunkHeader.split(";", 2)[0], 16);

		while (chunkSize > 0) {
			connection.read(chunkSize, out);
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
	}

	@Override
	public byte[] bytes() {
		if (sizeHint > Integer.MAX_VALUE) {
			throw new ContentTooLargeError(sizeHint, Integer.MAX_VALUE);
		}

		try {
			var bytes = new ByteArrayOutputStream(sizeHint > 0L ? (int) sizeHint : 8192);
			transferTo(bytes);
			return bytes.toByteArray();
		} catch (IOException ex) {
			throw new UnprocessableContentError("Failed to read chunked request body", ex);
		}
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
