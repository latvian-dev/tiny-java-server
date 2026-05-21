package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public record ChunkedBody(ByteChannelConnection connection, String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() throws IOException {
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
}
