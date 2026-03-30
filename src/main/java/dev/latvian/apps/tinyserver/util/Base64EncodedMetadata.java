package dev.latvian.apps.tinyserver.util;

import dev.latvian.apps.tinyserver.OptionalString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public record Base64EncodedMetadata(Map<String, byte[]> map) {
	public static final byte[] NO_DATA = new byte[0];
	public static final Base64EncodedMetadata EMPTY = new Base64EncodedMetadata(Map.of());

	@Nullable
	public byte[] get(String key) {
		return map.get(key);
	}

	public OptionalString getString(String key) {
		var data = get(key);
		return data == null ? OptionalString.MISSING : data.length == 0 ? OptionalString.EMPTY : OptionalString.of(new String(data, StandardCharsets.UTF_8));
	}

	public String encode() {
		var builder = new StringBuilder();
		boolean first = true;

		for (var entry : map.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}

			if (first) {
				first = false;
			} else {
				builder.append(',');
			}

			builder.append(entry.getKey());

			if (entry.getValue().length > 0) {
				builder.append('=');
				builder.append(Base64.getEncoder().encodeToString(entry.getValue()));
			}
		}

		return builder.toString();
	}

	@Override
	public @NotNull String toString() {
		return encode();
	}
}