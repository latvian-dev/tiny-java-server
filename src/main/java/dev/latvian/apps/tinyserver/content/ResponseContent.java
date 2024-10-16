package dev.latvian.apps.tinyserver.content;

import dev.latvian.apps.tinyserver.HTTPConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;

public interface ResponseContent {
	default long length() {
		return -1L;
	}

	default String type() {
		return "";
	}

	void write(OutputStream out) throws IOException;

	default byte[] toBytes() throws IOException {
		var out = new ByteArrayOutputStream();
		write(out);
		return out.toByteArray();
	}

	default void transferTo(HTTPConnection<?> connection) throws IOException {
		connection.write(ByteBuffer.wrap(toBytes()));
	}

	default HttpRequest.BodyPublisher bodyPublisher() throws IOException {
		return HttpRequest.BodyPublishers.ofByteArray(toBytes());
	}
}
