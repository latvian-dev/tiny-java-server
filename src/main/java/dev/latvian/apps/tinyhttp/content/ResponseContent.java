package dev.latvian.apps.tinyhttp.content;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.http.response.error.client.ContentTooLargeError;
import dev.latvian.apps.tinyhttp.http.response.error.client.RangeNotSatisfiableError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;

public interface ResponseContent {
	default long length() {
		return -1L;
	}

	default long rangeLength() {
		return length();
	}

	default String type() {
		return "";
	}

	default String actualType() {
		var type = type();
		return type == null || type.isEmpty() ? MimeType.OCTET_STREAM : type;
	}

	void write(OutputStream out) throws IOException;

	default byte[] toBytes() throws IOException {
		long len = rangeLength();

		if (len > Integer.MAX_VALUE) {
			throw new ContentTooLargeError(len, Integer.MAX_VALUE);
		}

		var out = new ByteArrayOutputStream((int) len);
		write(out);
		return out.toByteArray();
	}

	void transferTo(HTTPConnection<?> connection) throws IOException;

	HttpRequest.BodyPublisher bodyPublisher() throws IOException;

	default ResponseContent withRange(RequestRange range) {
		throw new RangeNotSatisfiableError(0L);
	}
}
