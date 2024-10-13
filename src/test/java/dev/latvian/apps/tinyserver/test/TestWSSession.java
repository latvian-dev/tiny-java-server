package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.ws.WSSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestWSSession extends WSSession<TestRequest> {
	@Override
	public void onOpen(TestRequest req) {
		sendText("Hello from " + id() + "! " + req.variables() + ", " + req.headers());
	}

	@Override
	public void onClose(StatusCode reason, boolean remote) {
		System.out.println("WS " + id() + " Closed: " + reason + ", remote: " + remote);
	}

	@Override
	public void onTextMessage(String message) {
		System.out.println("WS: " + message);
	}

	@Override
	public void onPing(ByteBuffer payload) {
		System.out.println("WS Ping: " + StandardCharsets.UTF_8.decode(payload.duplicate()));
	}
}
