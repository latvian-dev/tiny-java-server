package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.OptionalString;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

	ByteBuffer byteBuffer() throws IOException;

	default void byteBuffer(ByteBuffer to) throws IOException {
		to.put(to.position(), byteBuffer(), 0, to.remaining());
	}

	default String contentType() {
		return "";
	}

	default long transferTo(OutputStream out) throws IOException {
		var bytes = bytes();
		out.write(bytes);
		return bytes.length;
	}

	default String text() throws IOException {
		return StandardCharsets.UTF_8.decode(byteBuffer()).toString();
	}

	default byte[] bytes() throws IOException {
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

	default Map<String, OptionalString> getPostData() throws IOException {
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

	@Nullable
	default Body nextBody() {
		return null;
	}
}

