package dev.latvian.apps.tinyserver.http.body;

import dev.latvian.apps.tinyserver.OptionalString;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Body {
	static void appendQuotedString(StringBuilder builder, String value) {
		builder.append('"');

		int len = value.length();

		for (int i = 0; i < len; ++i) {
			char c = value.charAt(i);

			switch (c) {
				case '\n' -> builder.append("%0A");
				case '\r' -> builder.append("%0D");
				case '"' -> builder.append("%22");
				default -> builder.append(c);
			}
		}

		builder.append('"');
	}

	ByteBuffer byteBuffer();

	default String text() {
		return StandardCharsets.UTF_8.decode(byteBuffer()).toString();
	}

	default byte[] bytes() {
		var buf = byteBuffer();

		try {
			return buf.array();
		} catch (Exception ex) {
			var out = new ByteArrayOutputStream(buf.remaining());

			while (buf.hasRemaining()) {
				out.write(buf.get());
			}

			return out.toByteArray();
		}
	}

	default OptionalString property(String key) {
		return OptionalString.MISSING;
	}

	default String name() {
		return "";
	}

	default String fileName() {
		return "";
	}

	default String contentType() {
		return "";
	}

	default Map<String, OptionalString> getPostData() {
		var text = text();

		if (text.isEmpty()) {
			return Collections.emptyMap();
		}

		var map = new LinkedHashMap<String, OptionalString>(4);

		for (var s : text.split("&")) {
			var p = s.split("=", 2);

			try {
				var k = URLDecoder.decode(p[0], StandardCharsets.UTF_8);

				if (!k.isEmpty()) {
					if (p.length == 2) {
						map.put(k, OptionalString.of(URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
					} else {
						map.put(k, OptionalString.EMPTY);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return map;
	}
}

