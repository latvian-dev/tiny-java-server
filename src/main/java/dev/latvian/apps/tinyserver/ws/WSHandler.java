package dev.latvian.apps.tinyserver.ws;

import dev.latvian.apps.tinyserver.http.HTTPRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public interface WSHandler<REQ extends HTTPRequest, WSS extends WSSession<REQ>> {
	static <REQ extends HTTPRequest, WSS extends WSSession<REQ>> WSHandler<REQ, WSS> empty() {
		return (WSHandler) EmptyWSHandler.INSTANCE;
	}

	Map<UUID, WSS> sessions();

	default void broadcastText(String payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = new WSPayload(true, payload.getBytes(StandardCharsets.UTF_8));

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastText(Supplier<String> payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = new WSPayload(true, payload.get().getBytes(StandardCharsets.UTF_8));

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(byte[] payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = new WSPayload(false, payload);

			for (var session : s) {
				session.send(p);
			}
		}
	}

	default void broadcastBinary(Supplier<byte[]> payload) {
		var s = sessions().values();

		if (!s.isEmpty()) {
			var p = new WSPayload(false, payload.get());

			for (var session : s) {
				session.send(p);
			}
		}
	}
}
