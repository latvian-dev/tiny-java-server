package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.HTTPConnection;
import dev.latvian.apps.tinyserver.HTTPServer;

import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.time.Instant;

public class TestConnection extends HTTPConnection {
	public TestConnection(HTTPServer<?> server, SocketChannel socketChannel, Instant createdTime) {
		super(server, socketChannel, createdTime);
	}

	@Override
	protected void beforeHandshake() {
		System.out.println("\u001B[32mWaiting handshake " + this + "\u001B[0m");
	}

	@Override
	protected void closed() {
		System.out.println("\u001B[34mClosed connection from " + this + "\u001B[0m");
	}

	@Override
	protected void error(Throwable error) {
		if (error instanceof SocketTimeoutException || error instanceof ClosedChannelException) {
			System.out.println("\u001B[31mConnection " + this + " timed out\u001B[0m");
		} else {
			error.printStackTrace();
		}
	}
}
