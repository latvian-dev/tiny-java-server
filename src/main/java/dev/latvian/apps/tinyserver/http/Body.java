package dev.latvian.apps.tinyserver.http;

import dev.latvian.apps.tinyserver.OptionalString;
import dev.latvian.apps.tinyserver.content.MimeType;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Body {
	ByteBuffer byteBuffer;
	Map<String, OptionalString> properties; // new LinkedHashMap<>()
	String name = "";
	String fileName = "";
	String contentType = MimeType.TEXT;

	public ByteBuffer byteBuffer() {
		return byteBuffer.position(0);
	}

	public String text() {
		return StandardCharsets.UTF_8.decode(byteBuffer()).toString();
	}

	public byte[] bytes() {
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

	public OptionalString property(String key) {
		return properties == null || properties.isEmpty() ? OptionalString.MISSING : properties.getOrDefault(key.toLowerCase(), OptionalString.MISSING);
	}

	public String name() {
		return name;
	}

	public String fileName() {
		return fileName;
	}

	public String contentType() {
		return contentType;
	}

	public Map<String, OptionalString> getPostData() {
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

	@Override
	public String toString() {
		return name.isEmpty() ? "body" : name;
	}
}

