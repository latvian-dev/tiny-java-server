package dev.latvian.apps.tinyserver.content;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
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
	public HttpRequest.BodyPublisher bodyPublisher() throws IOException {
		return HttpRequest.BodyPublishers.ofByteArray(Files.readAllBytes(file));
	}
}
