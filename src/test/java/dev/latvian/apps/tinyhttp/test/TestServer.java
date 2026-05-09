package dev.latvian.apps.tinyhttp.test;

import dev.latvian.apps.tinyhttp.HTTPConnection;
import dev.latvian.apps.tinyhttp.HTTPServer;

import java.nio.channels.SocketChannel;
import java.time.Instant;

public class TestServer extends HTTPServer<TestRequest> {
	public TestServer() {
		super(TestRequest::new);
	}

	@Override
	protected void serverStarted() {
		System.out.println("Server started");
	}

	@Override
	protected void serverStopped(Throwable ex) {
		System.out.println("Server stopped");
		super.serverStopped(ex);
	}

	@Override
	protected HTTPConnection<TestRequest> createConnection(SocketChannel socketChannel, Instant createdTime) {
		return new TestConnection(this, socketChannel, createdTime);
	}
}
