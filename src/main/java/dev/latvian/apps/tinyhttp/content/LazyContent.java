package dev.latvian.apps.tinyhttp.content;

import dev.latvian.apps.tinyhttp.HTTPConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.util.function.Supplier;

public class LazyContent implements ResponseContent {
	private final Supplier<ResponseContent> content;
	private final long knownLength;
	private ResponseContent cached;

	public LazyContent(Supplier<ResponseContent> content, long knownLength) {
		this.content = content;
		this.knownLength = knownLength;
		this.cached = null;
	}

	public LazyContent(Supplier<ResponseContent> content) {
		this(content, -1L);
	}

	public ResponseContent get() {
		if (cached == null) {
			cached = content.get();
		}

		return cached;
	}

	@Override
	public long length() {
		return knownLength == -1L ? get().length() : knownLength;
	}

	@Override
	public long rangeLength() {
		return get().rangeLength();
	}

	@Override
	public String type() {
		return get().type();
	}

	@Override
	public void write(OutputStream out) throws IOException {
		get().write(out);
	}

	@Override
	public byte[] toBytes() throws IOException {
		return get().toBytes();
	}

	@Override
	public void transferTo(HTTPConnection<?> connection) throws IOException {
		get().transferTo(connection);
	}

	@Override
	public HttpRequest.BodyPublisher bodyPublisher() throws IOException {
		return get().bodyPublisher();
	}

	@Override
	public ResponseContent withRange(RequestRange range) {
		return get().withRange(range);
	}
}
