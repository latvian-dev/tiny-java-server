package dev.latvian.apps.tinyhttp.http.response.encoding;

import dev.latvian.apps.tinyhttp.content.ByteContent;
import dev.latvian.apps.tinyhttp.content.ResponseContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class DeflateResponseContentEncoding implements ResponseContentEncoding {
	public static final DeflateResponseContentEncoding INSTANCE = new DeflateResponseContentEncoding();

	private DeflateResponseContentEncoding() {
	}

	@Override
	public String name() {
		return "deflate";
	}

	@Override
	public ResponseContent encode(ResponseContent body) throws IOException {
		var out = new ByteArrayOutputStream();

		try (var stream = new DeflaterOutputStream(out)) {
			body.write(stream);
		}

		return new ByteContent(out.toByteArray(), body.type());
	}
}
