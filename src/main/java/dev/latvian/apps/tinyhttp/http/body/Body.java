package dev.latvian.apps.tinyhttp.http.body;

import dev.latvian.apps.tinyhttp.FormData;
import dev.latvian.apps.tinyhttp.NamedString;
import dev.latvian.apps.tinyhttp.http.response.error.client.UnprocessableContentError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

	default String contentType() {
		return "";
	}

	default long contentLength() {
		return -1L;
	}

	default void transferTo(OutputStream out) throws IOException {
		var buffer = byteBuffer();

		if (!buffer.hasRemaining()) {
			return;
		}

		var channel = Channels.newChannel(out);

		do {
			channel.write(buffer);
		}
		while (buffer.hasRemaining());
	}

	default String text() {
		return StandardCharsets.UTF_8.decode(byteBuffer()).toString();
	}

	default byte[] bytes() {
		var buf = byteBuffer();

		if (buf.hasArray()) {
			var result = buf.array();
			buf.position(buf.limit());
			return result;
		}

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

	default FormData formData() {
		var text = text();

		if (text.isEmpty()) {
			return FormData.EMPTY;
		}

		var values = new ArrayList<NamedString>(4);

		for (var s : text.split("&")) {
			var p = s.split("=", 2);

			try {
				var k = URLDecoder.decode(p[0], StandardCharsets.UTF_8);

				if (!k.isEmpty()) {
					if (p.length == 2) {
						values.add(NamedString.of(k, URLDecoder.decode(p[1], StandardCharsets.UTF_8)));
					} else {
						values.add(NamedString.empty(k));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return values.isEmpty() ? FormData.EMPTY : new FormData(values, List.of());
	}

	default BufferedImage image() {
		try (var in = new ByteArrayInputStream(bytes())) {
			return ImageIO.read(in);
		} catch (Exception ex) {
			throw new UnprocessableContentError("Failed to read the request body as an image", ex);
		}
	}
}

