package dev.latvian.apps.tinyhttp.test;

import dev.latvian.apps.tinyhttp.CloseReason;
import dev.latvian.apps.tinyhttp.ws.Frame;
import dev.latvian.apps.tinyhttp.ws.WSSession;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
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
		System.out.println("[" + Instant.now() + "] WS Text: " + payload);
		return null;
	}

	@Override
	@Nullable
	public Frame onBinaryMessage(ByteBuffer payload) {
		var sb = new StringBuilder("[").append(Instant.now()).append("] WS Binary: ");

		while (payload.hasRemaining()) {
			sb.append(" %02X".formatted(payload.get() & 0xFF));
		}

		System.out.println(sb);
		return null;
	}

	@Override
	public void onPing(ByteBuffer payload) {
		System.out.println("[" + Instant.now() + "] WS Ping: " + StandardCharsets.UTF_8.decode(payload));
	}

	@Override
	public void onPong(ByteBuffer payload) {
		System.out.println("[" + Instant.now() + "] WS Pong: " + StandardCharsets.UTF_8.decode(payload));
	}
}
