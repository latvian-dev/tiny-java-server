package dev.latvian.apps.tinyhttp.content;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.Arrays;

public record ByteContent(byte[] bytes, String type, @Nullable RequestRange range) implements ResponseContent {
	public ByteContent(byte[] bytes, String type) {
		this(bytes, type, null);
	}

	@Override
	public long length() {
		return bytes.length;
	}

	@Override
	public long rangeLength() {
		return range == null ? bytes.length : range.length();
	}

	@Override
	public void write(OutputStream out) throws IOException {
		if (range == null) {
			out.write(bytes);
		} else {
			out.write(bytes, (int) range.start(), (int) range.length());
		}
	}

	@Override
	public byte[] toBytes() {
		if (range == null) {
			return bytes;
		} else {
			return Arrays.copyOfRange(bytes, (int) range.start(), (int) (range.end() + 1L));
		}
	}

	@Override
	public void transferTo(HTTPConnection<?> connection) throws IOException {
		if (range == null) {
			connection.write(ByteBuffer.wrap(bytes));
		} else {
			connection.write(ByteBuffer.wrap(bytes, (int) range.start(), (int) range.length()));
		}
	}

	@Override
	public HttpRequest.BodyPublisher bodyPublisher() {
		if (range == null) {
			return HttpRequest.BodyPublishers.ofByteArray(bytes);
		} else {
			return HttpRequest.BodyPublishers.ofByteArray(bytes, (int) range.start(), (int) range.length());
		}
	}

	@Override
	public ResponseContent withRange(RequestRange range) {
		return new ByteContent(bytes, type, range);
	}
}
