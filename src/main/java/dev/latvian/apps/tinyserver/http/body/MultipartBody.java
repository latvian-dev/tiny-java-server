package dev.latvian.apps.tinyserver.http.body;

import dev.latvian.apps.tinyserver.OptionalString;

import java.nio.ByteBuffer;
import java.util.Map;

public record MultipartBody(ByteBuffer byteBuffer, Map<String, OptionalString> properties, String name, String fileName, String contentType) implements Body {
	@Override
	public ByteBuffer byteBuffer() {
		return byteBuffer.position(0);
	}

	@Override
	public OptionalString property(String key) {
		return properties == null || properties.isEmpty() ? OptionalString.MISSING : properties.getOrDefault(key.toLowerCase(), OptionalString.MISSING);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String fileName() {
		return fileName;
	}

	@Override
	public String contentType() {
		return contentType;
	}

	@Override
	public String toString() {
		return name.isEmpty() ? "multipart_body" : name;
	}
}
