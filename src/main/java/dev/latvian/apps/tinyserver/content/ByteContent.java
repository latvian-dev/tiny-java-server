package dev.latvian.apps.tinyserver.content;

import java.io.IOException;
import java.io.OutputStream;

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
	public byte[] toBytes() {
		return bytes;
	}
}
