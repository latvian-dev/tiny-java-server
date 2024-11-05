package dev.latvian.apps.tinyserver.content;

import dev.latvian.apps.tinyserver.HTTPConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public record FileContent(Path file, String overrideType) implements ResponseContent {
	@Override
	public long length() {
		try {
			return Files.size(file);
		} catch (IOException ignore) {
			return -1L;
		}
	}

	@Override
	public String type() {
		if (overrideType == null || overrideType.isEmpty()) {
			try {
				return Files.probeContentType(file);
			} catch (IOException ignore) {
				return "";
			}
		}

		return overrideType;
	}

	@Override
	public void write(OutputStream out) throws IOException {
		Files.copy(file, out);
	}

	@Override
	public byte[] toBytes() throws IOException {
		return Files.readAllBytes(file);
	}

	@Override
	public void transferTo(HTTPConnection<?> connection) throws IOException {
		try (var fileChannel = Files.newByteChannel(file)) {
			var buf = ByteBuffer.allocate(8192);

			while (fileChannel.read(buf) != -1) {
				buf.flip();
				connection.write(buf);
				buf.clear();
			}
		}
	}

	@Override
	public boolean hasData() {
		return Files.exists(file) && Files.isRegularFile(file) && Files.isReadable(file);
	}
}
