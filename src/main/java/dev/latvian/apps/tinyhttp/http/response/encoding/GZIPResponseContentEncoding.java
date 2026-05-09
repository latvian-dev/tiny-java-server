package dev.latvian.apps.tinyhttp.http.response.encoding;

import dev.latvian.apps.tinyhttp.content.ByteContent;
import dev.latvian.apps.tinyhttp.content.ResponseContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPResponseContentEncoding implements ResponseContentEncoding {
	public static final GZIPResponseContentEncoding INSTANCE = new GZIPResponseContentEncoding();

	private GZIPResponseContentEncoding() {
	}

	@Override
	public String name() {
		return "gzip";
	}

	@Override
	public ResponseContent encode(ResponseContent body) throws IOException {
		var out = new ByteArrayOutputStream();

		try (var stream = new GZIPOutputStream(out)) {
			body.write(stream);
		}

		return new ByteContent(out.toByteArray(), body.type());
	}
}
