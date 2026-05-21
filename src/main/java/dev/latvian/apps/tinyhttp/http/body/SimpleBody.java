package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.util.ByteChannelConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public record SimpleBody(ByteChannelConnection connection, int contentLength, String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() throws IOException {
		var bodyBuffer = ByteBuffer.allocate(contentLength);
		connection().read(bodyBuffer);
		return bodyBuffer.flip();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return Channels.newInputStream(connection.getChannel()).transferTo(out);
	}

	@Override
	public String toString() {
		return "body";
	}
}
