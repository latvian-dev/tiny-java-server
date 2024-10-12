package dev.latvian.apps.tinyserver.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;

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

	default HttpRequest.BodyPublisher bodyPublisher() throws IOException {
		throw new IllegalStateException("Body publisher not supported");
	}
}
