package dev.latvian.apps.tinyserver.content;

import java.io.IOException;
import java.io.OutputStream;

public record ByteContent(byte[] bytes, String type) implements ResponseContent {
	public static final ByteContent EMPTY = new ByteContent(new byte[0], "");

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

	@Override
	public boolean hasData() {
		return bytes.length > 0;
	}
}
