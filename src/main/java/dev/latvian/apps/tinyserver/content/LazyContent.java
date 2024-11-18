package dev.latvian.apps.tinyserver.content;

import dev.latvian.apps.tinyserver.HTTPConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.util.function.Supplier;

public class LazyContent implements ResponseContent {
	private final Supplier<ResponseContent> content;
	private ResponseContent cached;

	public LazyContent(Supplier<ResponseContent> content) {
		this.content = content;
		this.cached = null;
	}

	public ResponseContent get() {
		if (cached == null) {
			cached = content.get();
		}

		return cached;
	}

	@Override
	public long length() {
		return get().length();
	}

	@Override
	public String type() {
		return get().type();
	}

	@Override
	public boolean hasData() {
		return get().hasData();
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
}
