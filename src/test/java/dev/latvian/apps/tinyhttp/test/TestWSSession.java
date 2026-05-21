package dev.latvian.apps.tinyhttp.test;

import dev.latvian.apps.tinyhttp.CloseReason;
import dev.latvian.apps.tinyhttp.ws.Frame;
import dev.latvian.apps.tinyhttp.ws.WSSession;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class TestWSSession extends WSSession<TestRequest> {
	public final TestRequest req;

	public TestWSSession(TestRequest req) {
		this.req = req;
	}

	@Override
	public void onOpen() {
		System.out.println("WS " + key() + " Connected from " + req.connection());
		send(Frame.text("Hello from " + key() + "! " + req.variables() + ", " + req.headers()));
	}

	@Override
	public void onClose(CloseReason reason, @Nullable Throwable error) {
		System.out.println("WS " + key() + " Closed: " + reason.status() + ", remote: " + reason.remote() + ", error: " + error);

		if (error != null) {
			error.printStackTrace();
		}
	}

	@Override
	@Nullable
	public Frame onTextMessage(String payload) {
		System.out.println("WS: " + payload);
		return null;
	}

	@Override
	public void onPing(byte[] payload) {
		System.out.println("[" + Instant.now() + "] WS Ping: " + new String(payload, StandardCharsets.UTF_8));
	}

	@Override
	public void onPong(byte[] payload) {
		System.out.println("[" + Instant.now() + "] WS Pong: " + new String(payload, StandardCharsets.UTF_8));
	}
}
