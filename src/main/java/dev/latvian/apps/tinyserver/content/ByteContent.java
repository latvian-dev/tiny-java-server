package dev.latvian.apps.tinyserver.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;

public record ByteContent(byte[] bytes, String type) implements ResponseContent {
	@Override
	public long length() {
		return bytes.length;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		out.write(bytes);
	}

	@Override
	public byte[] toBytes() throws IOException {
		return bytes;
	}

	@Override
	public HttpRequest.BodyPublisher bodyPublisher() {
		return HttpRequest.BodyPublishers.ofByteArray(bytes);
	}
}
