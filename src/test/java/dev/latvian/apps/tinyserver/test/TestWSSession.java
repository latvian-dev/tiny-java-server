package dev.latvian.apps.tinyserver.test;

import dev.latvian.apps.tinyserver.StatusCode;
import dev.latvian.apps.tinyserver.ws.WSSession;

import java.nio.charset.StandardCharsets;

public class TestWSSession extends WSSession<TestRequest> {
	@Override
	public void onOpen(TestRequest req) {
		System.out.println("WS " + id() + " Connected from " + req.connection());
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
	public void onPing(byte[] payload) {
		System.out.println("WS Ping: " + new String(payload, StandardCharsets.UTF_8));
	}

	@Override
	public void onPong(byte[] payload) {
		System.out.println("WS Pong: " + new String(payload, StandardCharsets.UTF_8));
	}
}
