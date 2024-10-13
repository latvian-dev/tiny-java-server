package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.HTTPServer;

import java.nio.channels.SocketChannel;
import java.time.Instant;

public class TestServer extends HTTPServer<TestRequest> {
	public TestServer() {
		super(TestRequest::new);
	}

	@Override
	protected HTTPConnection createConnection(SocketChannel socketChannel, Instant createdTime) {
		return new TestConnection(this, socketChannel, createdTime);
	}
}
